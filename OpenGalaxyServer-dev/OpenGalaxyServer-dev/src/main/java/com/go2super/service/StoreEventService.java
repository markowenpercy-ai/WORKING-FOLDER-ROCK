package com.go2super.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.go2super.database.cache.StoreEventCache;
import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.Account;
import com.go2super.database.entity.StoreEvent;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.StoreEventEntry;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.database.entity.sub.storeevent.BuyEventEntry;
import com.go2super.dto.CMSDTO;
import com.go2super.dto.cms.Iv;
import com.go2super.dto.cms.Token;
import com.go2super.dto.cms.global.CMSEventList;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import okhttp3.*;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class StoreEventService {

    private static StoreEventService instance;

    @Getter
    private final UserCache userCache;
    @Getter
    private final StoreEventCache eventCache;
    @Value("${application.cms}")
    private String Url;
    @Value("${application.cms-client-id}")
    private String clientId;
    @Value("${application.cms-client-secret}")
    private String clientSecret;
    private String token;
    private Dictionary<String, LocalDateTime> fetchDataTime;
    private Dictionary<String, CMSDTO> cmsCache;
    @Autowired
    public StoreEventService(UserCache userCache, StoreEventCache eventCache) {
        instance = this;
        this.userCache = userCache;
        this.eventCache = eventCache;
        this.cmsCache = new Hashtable<>();
        this.fetchDataTime = new Hashtable<>();
    }

    public StoreEvent getStoreEvent(String accountId) {
        Optional<StoreEvent> eventOptional = this.eventCache.findById(accountId);
        if (eventOptional.isEmpty()) {
            StoreEvent event = new StoreEvent();

            event.setAccountId(accountId);
            event.setEvents(new ArrayList<>());

            this.eventCache.save(event);
            return event;
        } else {
            return eventOptional.get();
        }
    }

    public StoreEventEntry getEventEntry(StoreEvent event, String guid, StoreEventEntry elseUse) {
        for (StoreEventEntry entry : event.getEvents()) {
            if (guid.equals(entry.getGuid())) {
                return entry;
            }
        }

        event.getEvents().add(elseUse);
        this.eventCache.save(event);
        return elseUse;
    }

    public long getStorePoints(Account account) {
        String accountId = account.getId().toString();
        StoreEvent accountEvent = this.getStoreEvent(accountId);
        return accountEvent.getStorePoints();
    }

    public void addStorePoints(User user, int storePoints) {
        String accountId = user.getAccountId();
        StoreEvent accountEvent = this.getStoreEvent(accountId);
        accountEvent.setStorePoints(accountEvent.getStorePoints() + storePoints);
        eventCache.save(accountEvent);
    }

    public BuyEventEntry GetEventEntry(User user,String guid){
        String accountId = user.getAccountId();
        StoreEvent accountEvent = this.getStoreEvent(accountId);
        BuyEventEntry buyEvent = (BuyEventEntry) this.getEventEntry(
                accountEvent,
                guid,
                new BuyEventEntry(guid)
        );
        return buyEvent;
    }

    public int spinWheel(User user, String guid) throws IOException {
        CMSDTO cmsResponse = GetCMSResponse(guid, false);
        String accountId = user.getAccountId();
        StoreEvent accountEvent = this.getStoreEvent(accountId);
        long newPoints = accountEvent.getStorePoints() - 100;
        if (newPoints < 0) {
            throw new InvalidParameterException("Not enough points");
        }
        float weightSum = 0F;
        var rndReward = cmsResponse.getData().getFindEventitemsContent().getData().getRandom().getIv();
        for(var reward : rndReward){
            weightSum += reward.getWeight();
        }
        float random = new SecureRandom().nextFloat() * weightSum;
        float lowerRangeLimit = 0;
        float upperRangeLimit;
        int selectedIndex = -1;
        Iv currentSelected = null;
        UserEmailStorage userEmailStorage = user.getUserEmailStorage();
        Email email = Email.builder()
                .autoId(userEmailStorage.nextAutoId())
                .type(2)
                .name("System")
                .subject("Event Rewards")
                .emailContent("Commander, here is your purchase.")
                .readFlag(0)
                .date(DateUtil.now())
                .goods(new ArrayList<>())
                .guid(-1)
                .build();
        for (int j = 0; j < rndReward.size(); j++) {
            upperRangeLimit = lowerRangeLimit + rndReward.get(j).getWeight();
            if (random < upperRangeLimit) {
                currentSelected = rndReward.get(j);
                selectedIndex = j;
                break;
            }
            lowerRangeLimit = upperRangeLimit;
        }
        if(currentSelected == null){
            Collections.shuffle(rndReward);
            currentSelected = rndReward.get(0);
        }
        var randomprop = currentSelected.GetObject();
        if(randomprop == null){
            throw new InvalidParameterException("Unable to find any object within this ID: " + currentSelected.getItemId());
        }
        var existed = email.getGoods().stream().filter(x -> x.getGoodId() == randomprop.getId()).findFirst();
        if(existed.isPresent()){
            existed.get().setLockNum(existed.get().getLockNum() + 1);
        }
        else{
            email.addGood(
                    EmailGood.builder()
                            .goodId(randomprop.getId())
                            .lockNum(currentSelected.getCount())
                            .build()
            );
        }
        accountEvent.setStorePoints(newPoints);
        eventCache.save(accountEvent);
        userEmailStorage.addEmail(email);
        user.setToSave(true);
        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);

        if (gameUserOptional.isPresent()) {
            LoggedGameUser loggedGameUser = gameUserOptional.get();
            ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();

            loggedGameUser.getSmartServer().send(response);
        }
        return selectedIndex;
    }

    public void fetchFirst() throws IOException {
        if (Url == null || Url.isBlank()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        OkHttpClient client = new OkHttpClient();
        if(getInstance().token == null){
            RequestBody body = new FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("client_id", getInstance().clientId)
                    .add("client_secret", getInstance().clientSecret)
                    .add("scope", "squidex-api")
                    .build();
            Request request = new Request.Builder()
                    .url(Url + "/identity-server/connect/token")
                    .post(body)
                    .build();
            Response rt = client.newCall(request).execute();
            Token cmsResponse = mapper.readValue(rt.body().string(), Token.class);
            getInstance().token = cmsResponse.getToken_type() + " " + cmsResponse.getAccess_token();
        }
        Request request = new Request.Builder()
                .url(Url + "/api/content/bngo2/events")
                .header("Authorization", token)
                .get()
                .build();

        Response r = client.newCall(request).execute();
        String value = r.body().string();
        CMSEventList cmsResponse = mapper.readValue(value, CMSEventList.class);
        for(var item : cmsResponse.items){
            for(var iv: item.data.plannedEvent.iv){
                if(iv.eventId.size() > 0){
                    GetCMSResponse(iv.eventId.get(0), false);
                }
            }
        }
    }

    public boolean purchasePack(User user, int packId, int quantity, String guid) throws IOException {
        if (quantity <= 0 || quantity > 9999) {
            return false;
        }
        //fetch data from CMS
        CMSDTO cmsResponse = GetCMSResponse(guid, false);

        String accountId = user.getAccountId();
        StoreEvent accountEvent = this.getStoreEvent(accountId);
        List<Iv> packs = cmsResponse.getData().getFindEventitemsContent().getData().getData().getIv().stream().filter(x -> x.getItemId() == packId).toList();
        if (packs == null || packs.size() < 1) {
            throw new InvalidParameterException("No such goods found!");
        }
        Iv pack = packs.get(0);
        long newPoints = accountEvent.getStorePoints() - (long) pack.getPrice() * quantity;
        if (newPoints < 0) {
            throw new InvalidParameterException("Not enough points");
        }

        BuyEventEntry buyEvent = (BuyEventEntry) this.getEventEntry(
            accountEvent,
            guid,
            new BuyEventEntry(guid)
        );
        if (pack.getLimit() != -1) {
            if (pack.getLimit() < buyEvent.getLimit(packId) + quantity) {
                throw new InvalidParameterException("Purchase Limit Reached!");
            }
        }
        UserEmailStorage userEmailStorage = user.getUserEmailStorage();
        Email email = Email.builder()
            .autoId(userEmailStorage.nextAutoId())
            .type(2)
            .name("System")
            .subject("Event Rewards")
            .emailContent("Commander, here is your purchase.")
            .readFlag(0)
            .date(DateUtil.now())
            .goods(new ArrayList<>())
            .guid(-1)
            .build();

        var prop = pack.GetObject();
        email.addGood(
                EmailGood.builder()
                        .goodId(prop.getId())
                        .lockNum(quantity * pack.getCount())
                        .build()
        );
        //get random
        if(!cmsResponse.getData().getFindEventitemsContent().getData().getRandom().getIv().isEmpty()){
            float weightSum = 0F;
            var rndReward = cmsResponse.getData().getFindEventitemsContent().getData().getRandom().getIv();
            for(var reward : rndReward){
                weightSum += reward.getWeight();
            }

            for(int i = 0; i < quantity; i++){
                float lowerRangeLimit = 0;
                float upperRangeLimit;
                Iv currentSelected = null;
                float random = new SecureRandom().nextFloat() * weightSum;
                for (int j = 0; j < rndReward.size(); j++) {
                    upperRangeLimit = lowerRangeLimit + rndReward.get(j).getWeight();
                    if (random < upperRangeLimit) {
                        currentSelected = rndReward.get(j);
                        break;
                    }
                    lowerRangeLimit = upperRangeLimit;
                }
                if(currentSelected == null){
                    Collections.shuffle(rndReward);
                    currentSelected = rndReward.get(0);
                }
                var randomprop = currentSelected.GetObject();
                if(randomprop == null){
                    throw new InvalidParameterException("Unable to find any object within this ID: " + currentSelected.getItemId());
                }
                var existed = email.getGoods().stream().filter(x -> x.getGoodId() == randomprop.getId()).findFirst();
                if(existed.isPresent()){
                    existed.get().setLockNum(existed.get().getLockNum() + 1);
                }
                else{
                    email.addGood(
                            EmailGood.builder()
                                    .goodId(randomprop.getId())
                                    .lockNum(currentSelected.getCount())
                                    .build()
                    );
                }
            }
        }
        buyEvent.addToLimit(packId, quantity);
        accountEvent.setStorePoints(newPoints);
        eventCache.save(accountEvent);
        userEmailStorage.addEmail(email);
        user.setToSave(true);
        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);

        if (gameUserOptional.isPresent()) {
            LoggedGameUser loggedGameUser = gameUserOptional.get();
            ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                .errorCode(0)
                .build();

            loggedGameUser.getSmartServer().send(response);
        }
        return true;
    }

    public CMSDTO GetCMSResponse(String guid, boolean force) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        OkHttpClient client = new OkHttpClient();
        if(getInstance().token == null){
            RequestBody body = new FormBody.Builder()
                    .add("grant_type", "client_credentials")
                    .add("client_id", getInstance().clientId)
                    .add("client_secret", getInstance().clientSecret)
                    .add("scope", "squidex-api")
                    .build();
            Request request = new Request.Builder()
                    .url(Url + "/identity-server/connect/token")
                    .post(body)
                    .build();
            Response rt = client.newCall(request).execute();
            Token cmsResponse = mapper.readValue(rt.body().string(), Token.class);
            getInstance().token = cmsResponse.getToken_type() + " " + cmsResponse.getAccess_token();
            System.out.println("Fetching Event Data for " + guid);
        }
        else if(!force && getInstance().cmsCache.get(guid) != null){
            return cmsCache.get(guid);
        }
        String content = "{findEventitemsContent(id:\""+guid+"\"){data{eventType{iv}data{iv{price,itemId,limit,count}},random{iv{itemId,weight,count}}}}}";
        JSONObject object = new JSONObject();
        object.put("query", content);
        Request request = new Request.Builder()
                .url(Url + "/api/content/bngo2/graphql")
                .header("Authorization", token)
                .post(RequestBody.create(MediaType.parse("application/json"), object.toString()))
                .build();

        Response r = client.newCall(request).execute();
        String value = r.body().string();
        CMSDTO cmsResponse = mapper.readValue(value, CMSDTO.class);
        getInstance().cmsCache.put(guid, cmsResponse);
        return cmsResponse;
    }

    public static StoreEventService getInstance() {
        return instance;
    }
}

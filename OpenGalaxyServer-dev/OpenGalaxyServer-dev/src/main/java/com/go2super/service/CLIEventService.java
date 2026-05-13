package com.go2super.service;

import com.go2super.database.cache.StoreEventCache;
import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.StoreEvent;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.Email;
import com.go2super.database.entity.sub.EmailGood;
import com.go2super.database.entity.sub.StoreEventEntry;
import com.go2super.database.entity.sub.UserEmailStorage;
import com.go2super.database.entity.sub.storeevent.BuyEventEntry;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.json.storeevent.CommanderEventJson;
import com.go2super.resources.json.storeevent.PackData;
import com.go2super.resources.json.storeevent.RandomData;
import com.go2super.socket.util.DateUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CLIEventService {

    private static CLIEventService instance;

    @Getter
    private final UserCache userCache;
    @Getter
    private final StoreEventCache eventCache;
    private final CommanderEventJson commanderEventJson;
    private final AtomicBoolean eventEnabled = new AtomicBoolean(false);


    @Autowired
    public CLIEventService(UserCache userCache, StoreEventCache eventCache) {
        instance = this;
        this.userCache = userCache;
        this.eventCache = eventCache;
        this.commanderEventJson = ResourceManager.getCommanderEvent();
    }

    public boolean isEventEnabled() {
        return eventEnabled.get();
    }

    public void setEventEnabled(boolean enabled) {
        eventEnabled.set(enabled);
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

    public void resetStoreEventForAll() {
        for (User user : this.userCache.findAll()) {
            StoreEvent event = this.getStoreEvent(user.getAccountId());
            if (event == null || event.getEvents() == null) {
                continue;
            }
            for (StoreEventEntry entry : event.getEvents()) {
                if (entry.getGuid() == null || !entry.getGuid().equals(this.commanderEventJson.getEventId())) {
                    continue;
                }
                if (entry instanceof BuyEventEntry b && !CollectionUtils.isEmpty(b.getLimits())) {
                    b.getLimits().clear();
                }
            }
            this.eventCache.save(event);
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


    public CommanderEventJson getPackList() {
        return this.commanderEventJson;
    }


    public boolean purchasePack(User user, String packShortName, int quantity) {
        if (quantity <= 0 || quantity > 9999) {
            return false;
        }

        String accountId = user.getAccountId();
        StoreEvent accountEvent = this.getStoreEvent(accountId);
        CommanderEventJson pack = this.commanderEventJson;
        if (pack == null) {
            return false;
        }

        PackData packData = pack.findByShortName(packShortName);


        BuyEventEntry buyEvent = (BuyEventEntry) this.getEventEntry(
                accountEvent,
                pack.getEventId(),
                new BuyEventEntry(pack.getEventId())
        );
        if (packData.getLimit() != -1) {
            if (packData.getLimit() < buyEvent.getLimit(packData.getId()) + quantity) {
                return false;
            }
        }


        long newPoints = accountEvent.getStorePoints() - (long) packData.getCost() * quantity;
        if (newPoints < 0) {
            return false;
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

        email.addGood(
                EmailGood.builder()
                        .goodId(packData.getId())
                        .lockNum(packData.getCount() * quantity)
                        .build()
        );

        Map<Integer, Integer> rewardCountMap = new HashMap<>();

        for (int i = 0; i < quantity; i++) {
            RandomData reward = this.commanderEventJson.pickOne();
            int rewardId = reward.getId();
            int rewardCount = reward.getCount();
            rewardCountMap.put(rewardId, rewardCountMap.getOrDefault(rewardId, 0) + rewardCount);
        }

        for (Map.Entry<Integer, Integer> entry : rewardCountMap.entrySet()) {
            var existed = email.getGoods().stream().filter(x -> x.getGoodId() == entry.getKey()).findFirst();
            if(existed.isPresent()){
                existed.get().setLockNum(existed.get().getLockNum() + 1);
            }
            else{
                email.addGood(
                        EmailGood.builder()
                                .goodId(entry.getKey())
                                .lockNum(entry.getValue())
                                .build()
                );
            }
        }

        userEmailStorage.addEmail(email);
        Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);

        if (gameUserOptional.isPresent()) {
            LoggedGameUser loggedGameUser = gameUserOptional.get();
            ResponseNewEmailNoticePacket response = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();

            loggedGameUser.getSmartServer().send(response);
        }

        accountEvent.setStorePoints(newPoints);
        buyEvent.addToLimit(packData.getId(), quantity);
        eventCache.save(accountEvent);
        return true;
    }


    public static CLIEventService getInstance() {
        return instance;
    }
}
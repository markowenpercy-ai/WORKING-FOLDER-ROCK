package com.go2super.service;

import com.go2super.database.cache.TradeCache;
import com.go2super.database.entity.Account;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.Trade;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.TradeType;
import com.go2super.obj.game.TradeInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.utility.WideString;
import com.go2super.packet.custom.CustomWarnPacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.packet.mall.ResponseMyTradeInfoPacket;
import com.go2super.packet.mall.ResponseTradeInfoPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.props.PropCommanderData;
import com.go2super.service.trade.TradeAuditUserHistory;
import com.go2super.socket.util.DateUtil;
import com.go2super.utility.RomanNumber;
import lombok.Getter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.List;
import java.util.*;

@Getter
@Service
public class TradeService {

    public static int maxTradePage = 5;

    private static TradeService instance;

    @Getter
    private final TradeCache tradeCache;

    private final Map<Integer, TradeAuditUserHistory> tradeHistory;

    public TradeService(TradeCache tradeCache) {

        instance = this;

        this.tradeCache = tradeCache;

        this.tradeHistory = new HashMap<>();
    }

    public void logPageView(User user, int pageId) {

        int guid = user.getGuid();
        if (!this.tradeHistory.containsKey(guid)) {
            this.tradeHistory.put(guid, new TradeAuditUserHistory(
                user.getUsername(),
                user.getGuid()
            ));
        }

        TradeAuditUserHistory history = this.tradeHistory.get(guid);
        boolean flag = history.addEntry(pageId);

        if (flag) {
            DiscordService.getInstance().getRayoBot().sendAudit("Page View Warning", history.getViewedPagesFormatted(), Color.red, AuditType.TRADE);
            CustomWarnPacket popup = new CustomWarnPacket();
            popup.setName(WideString.of(user.getUsername(), 32));
            popup.setToName(WideString.of(user.getUsername(), 32));
            popup.setBuffer(WideString.of("Please do not spam the trade pages!", 1024));
            var gameUserOptional = LoginService.getInstance().getGame(user);
            gameUserOptional.ifPresent(x -> x.getSmartServer().send(popup));
        }
    }

    public void giveTrade(User user, User seller, Trade trade) {

        boolean isCancelling = trade.getSellerGuid() == user.getGuid();
        UserEmailStorage userEmailStorage = user.getUserEmailStorage();

        Email email = Email.builder()
            .autoId(userEmailStorage.nextAutoId())
            .name("System")
            .readFlag(0)
            .date(DateUtil.now())
            .goods(new ArrayList<>())
            .guid(-1)
            .build();

        if (isCancelling) {

            email.setSubject("Trade Cancelled");
            email.setEmailContent("Your goods have been returned from the global market, you can claim them by clicking on the icons.");

        } else {

            email.setSubject("Trade Completed");
            email.setEmailContent("You have bought goods in the store from " + seller.getUsername() + " (ID: " + seller.getGuid() + "), you can claim them by clicking on the icons.");

        }

        if (trade.getTradeType() == TradeType.SHIP) {
            TradeShip tradeShip = (TradeShip) trade;

            email.setType(3);
            email.addGood(EmailGood.builder()
                    .goodId(tradeShip.getShipModelId())
                    .num(trade.getAmount())
                    .build());
        } else {
            TradeItem tradeItem = (TradeItem) trade;

            email.setType(2);
            email.addGood(EmailGood.builder()
                    .goodId(tradeItem.getPropId())
                    .num(trade.getAmount())
                    .build());
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

        // Audit

        if (isCancelling) {
            return;
        }

        Account account = user.getAccount();
        Account sellerAccount = seller.getAccount();

        StringBuffer buffer = new StringBuffer();

        buffer.append("**Type:** `" + trade.getTradeType() + "`\n");
        buffer.append("**Price:** `" + trade.getPriceType().name() + " " + trade.getPrice() + "`\n");
        buffer.append("**Buyer:** `" + user.getUsername() + " (ID: " + user.getGuid() + ", EMAIL: " + account.getEmail() + ")`\n");
        buffer.append("**Seller:** `" + seller.getUsername() + " (ID: " + seller.getGuid() + ", EMAIL: " + sellerAccount.getEmail() + ")`\n\n");

        if (trade.getTradeType() == TradeType.SHIP) {
            TradeShip tradeShip = (TradeShip) trade;
            ShipModel shipModel = tradeShip.getShipModel();

            buffer.append("**Model:** `" + shipModel.getName() + " (ID: " + shipModel.getShipModelId() + ")`\n");
            buffer.append("**Body:** `" + shipModel.getBodyData().getName() + " " + RomanNumber.toRoman(shipModel.getBodyLevelMeta().getLv()) + "`\n");
            buffer.append("**Amount:** `" + trade.getAmount() + "`\n");
        } else {
            TradeItem tradeItem = (TradeItem) trade;
            int propId = tradeItem.getPropId();

            buffer.append("**Name:** `" + getTradeItemName(tradeItem) + " (ID: " + propId + ")`\n");
            buffer.append("**Amount:** `" + trade.getAmount() + "`\n");
        }

        DiscordService.getInstance().getRayoBot().sendAudit("Trade Information", buffer.toString(), Color.yellow, AuditType.TRADE);

    }

    public void giveAward(User user, String reason, int gold, int mp) {

        UserEmailStorage userEmailStorage = user.getUserEmailStorage();

        Email email = Email.builder()
            .autoId(userEmailStorage.nextAutoId())
            .name("System")
            .subject("Successful Sale")
            .emailContent(reason)
            .type(4)
            .readFlag(0)
            .date(DateUtil.now())
            .goods(new ArrayList<>())
            .guid(-1)
            .build();

        if (gold > 0) {

            email.addGood(EmailGood.builder()
                .goodId(0)
                .num(gold)
                .build());

        }

        if (mp > 0) {

            email.addGood(EmailGood.builder()
                .goodId(1)
                .num(mp)
                .build());

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

    }

    public ResponseMyTradeInfoPacket getMyTradeInfo(User user) {

        List<Trade> trades = tradeCache.findAllBySellerGuid(user.getGuid());

        List<TradeInfo> tradeInfos = getTradesPacketInfo(trades);
        ResponseMyTradeInfoPacket response = new ResponseMyTradeInfoPacket();

        response.setDataLen(tradeInfos.size());
        response.setTrades(tradeInfos);

        return response;

    }

    public ResponseTradeInfoPacket getTradeInfo(TradeType tradeType, int sellId, int pageId) {

        List<Trade> result;
        long count = 0;

        if (tradeType == TradeType.ALL) {
            result = tradeCache.findByPage(pageId, maxTradePage);
            count = tradeCache.count();
        } else {
            if (sellId >= 0) {
                result = tradeCache.findByPageAndTypeAndSellId(tradeType, sellId, pageId, maxTradePage);
                count = tradeCache.countByTypeAndSellId(tradeType, sellId);
            } else {
                result = tradeCache.findByPageAndType(tradeType, pageId, maxTradePage);
                count = tradeCache.countByType(tradeType);
            }
        }

        List<TradeInfo> tradeInfos = getTradesPacketInfo(result);
        ResponseTradeInfoPacket response = new ResponseTradeInfoPacket();

        response.setReserve(0);
        response.setTradeCount(Long.valueOf(count).intValue());
        response.setDataLen(tradeInfos.size());
        response.setTrades(tradeInfos);

        return response;

    }

    public List<TradeInfo> getTradesPacketInfo(List<Trade> trades) {

        List<TradeInfo> tradeInfos = new ArrayList<>();

        for (Trade trade : trades) {

            User seller = trade.getSeller();
            if (seller == null) {
                // System.out.println("TradeService.getTradesPacketInfo: seller is null (trade: " + trade + ")");
                continue;
            }

            TradeInfo tradeInfo = TradeInfo.builder()
                .sellUserId(seller.getUserId())
                .sellerGuid(seller.getGuid())
                .sellerName(seller.getUsername())
                .indexId(trade.getTradeId())
                .id(trade.getSellId())
                .num(trade.getAmount())
                .price(trade.getPrice())
                .spareTime(DateUtil.remains(trade.getUntil()).intValue())
                .reserve(0)
                .tradeType(trade.getTradeType().getCode())
                .priceType(trade.getPriceType().getCode())
                .build();

            if (trade.getTradeType() == TradeType.SHIP) {
                TradeShip tradeShip = (TradeShip) trade;
                tradeInfo.setId(tradeShip.getShipModelId());
                tradeInfo.setBodyId(tradeShip.getShipModel().getBodyId());
            } else {
                TradeItem tradeItem = (TradeItem) trade;
            }

            tradeInfos.add(tradeInfo);

        }

        return tradeInfos;

    }

    public String getTradeItemName(TradeItem tradeItem) {

        int propId = tradeItem.getPropId();
        boolean isCommander = false;

        PropData propData = null;

        // Commanders

        List<PropData> props = ResourceManager.getProps().getCommanders();

        for (PropData data : props) {
            if (data.hasCommanderData()) {
                if ((data.getId() + 8) >= propId && data.getId() <= propId) {
                    propData = data;
                    isCommander = true;
                    break;
                }
            }
        }

        if (propData == null) {
            for (PropData data : ResourceManager.getProps().getProps()) {
                if (data.getId() == propId) {
                    propData = data;
                    break;
                }
            }
        }

        if (propData != null) {

            if (isCommander) {

                PropCommanderData commanderData = propData.getCommanderData();
                return commanderData.getCommander().getName() + " " + ((propId - propData.getId()) + 1) + "*";

            }

            return propData.getName();

        }

        return "unknown";

    }

    public static TradeService getInstance() {

        return instance;
    }

}

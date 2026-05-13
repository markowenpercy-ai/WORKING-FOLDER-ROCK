package com.go2super.listener;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.Trade;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.PriceType;
import com.go2super.database.entity.type.TradeType;
import com.go2super.obj.game.Prop;
import com.go2super.obj.game.ShipModelInfo;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.game.TradeInfo;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.mall.*;
import com.go2super.packet.resource.RequestExchangePacket;
import com.go2super.packet.resource.ResponseExchangePacket;
import com.go2super.packet.shipmodel.ResponseShipModelInfoDelPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.BuildEffectMeta;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.resources.json.BuildsJson;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;

import java.util.*;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_ID;

public class TradeListener implements PacketListener {

    @PacketProcessor
    public void onTradeInfo(RequestTradeInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        // -1 = All
        // 0  = Ship
        // 2  = Blueprint
        // 3  = Card
        // 4  = Gem
        // 1  = Item
        TradeType type = TradeType.getByCode(packet.getKind());

        ResponseTradeInfoPacket response = TradeService.getInstance().getTradeInfo(type, packet.getId(), packet.getPageId());

        for (TradeInfo tradeInfo : response.getTrades()) {
            if (tradeInfo.getTradeType() == TradeType.SHIP.getCode()) {

                ShipModel shipModel = PacketService.getShipModel(tradeInfo.getId());
                if (shipModel == null) {
                    continue;
                }

                ResponseShipModelInfoDelPacket modelPacket = new ResponseShipModelInfoDelPacket();

                modelPacket.setDataLen(UnsignedShort.of(1));
                modelPacket.setShipModelInfoList(new ArrayList<>());

                modelPacket.getShipModelInfoList().add(
                    ShipModelInfo.of(
                        shipModel.getName(),
                        shipModel.partNum(),
                        shipModel.getShipModelId() == 0 ? 1 : 0,
                        shipModel.getBodyId(),
                        shipModel.partArray(),
                        shipModel.getShipModelId())
                );

                packet.reply(modelPacket);

            }
        }

        packet.reply(response);

        TradeService.getInstance().logPageView(user, packet.getPageId());
    }

    @PacketProcessor
    public void onMyTradeInfo(RequestMyTradeInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ResponseMyTradeInfoPacket response = TradeService.getInstance().getMyTradeInfo(user);

        for (TradeInfo tradeInfo : response.getTrades()) {
            if (tradeInfo.getTradeType() == TradeType.SHIP.getCode()) {

                ShipModel shipModel = PacketService.getShipModel(tradeInfo.getId());
                if (shipModel == null) {
                    continue;
                }

                ResponseShipModelInfoDelPacket modelPacket = new ResponseShipModelInfoDelPacket();

                modelPacket.setDataLen(UnsignedShort.of(1));
                modelPacket.setShipModelInfoList(new ArrayList<>());

                modelPacket.getShipModelInfoList().add(
                    ShipModelInfo.of(
                        shipModel.getName(),
                        shipModel.partNum(),
                        shipModel.getShipModelId() == 0 ? 1 : 0,
                        shipModel.getBodyId(),
                        shipModel.partArray(),
                        shipModel.getShipModelId())
                );

                packet.reply(modelPacket);

            }
        }

        packet.reply(response);

    }

    @PacketProcessor
    public void onDeleteTrade(RequestDeleteTradeGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getIndexId() < 0) {
            return;
        }

        Trade selectedTrade = TradeService.getInstance().getTradeCache().findByTradeId(packet.getIndexId());
        if (selectedTrade == null || selectedTrade.getSellerGuid() != user.getGuid()) {
            return;
        }

        TradeService.getInstance().getTradeCache().delete(selectedTrade);
        TradeService.getInstance().giveTrade(user, null, selectedTrade);

        user.save();

    }

    @PacketProcessor
    public void onBuyTrade(RequestBuyTradeGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        Trade selectedTrade = TradeService.getInstance().getTradeCache().findByTradeId(packet.getIndexId());
        if (selectedTrade == null || selectedTrade.getSellerGuid() != packet.getSellGuid()) {
            return;
        }

        User userSeller = selectedTrade.getSeller();
        if (userSeller == null) {
            return;
        }

        UserResources userResources = user.getResources();

        int sellerSendGold = 0;
        int sellerSendMp = 0;

        switch (selectedTrade.getPriceType()) {

            case MP:

                if (userResources.getMallPoints() < selectedTrade.getPrice()) {
                    return;
                }

                userResources.setMallPoints(userResources.getMallPoints() - selectedTrade.getPrice());
                sellerSendMp += selectedTrade.getPrice();
                break;

            case GOLD:

                if (userResources.getGold() < selectedTrade.getPrice()) {
                    return;
                }

                userResources.setGold(userResources.getGold() - selectedTrade.getPrice());
                sellerSendGold += selectedTrade.getPrice();
                break;

        }

        String reason = "You have successfully sold your goods, buyer: " + user.getUsername() + " (ID: " + user.getGuid() + ")";

        TradeService.getInstance().getTradeCache().delete(selectedTrade);
        TradeService.getInstance().giveAward(userSeller, reason, sellerSendGold, sellerSendMp);
        TradeService.getInstance().giveTrade(user, userSeller, selectedTrade);

        userSeller.getMetrics().add("action:trade", 1);

        user.update();
        userSeller.update();

        user.save();
        userSeller.save();

        ResponseBuyTradeGoodsPacket response = new ResponseBuyTradeGoodsPacket();

        response.setErrorCode(0);
        response.setSellGuid(packet.getSellGuid());
        response.setPriceType(selectedTrade.getPriceType() == PriceType.GOLD ? 0 : 1);
        response.setIndexId(packet.getIndexId());
        response.setPrice(selectedTrade.getPrice());

        packet.reply(response);

    }

    @PacketProcessor
    public void onTradeGoods(RequestTradeGoodsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuilding tradeBuilding = user.getBuildings().pickOne("build:trade");
        if (tradeBuilding == null) {
            return;
        }

        if (packet.getPrice() <= 0 || packet.getNum() <= 0) {
            return;
        }

        BuildLevelMeta buildLevelMeta = tradeBuilding.getLevelData();

        // * Intended: (int) buildLevelMeta.getType("transactions").getValue();
        // * Hardcoded: 30
        int maxTransactions = 30;

        List<Trade> currentTrades = TradeService.getInstance().getTradeCache().findAllBySellerGuid(packet.getGuid());

        if (currentTrades.size() >= maxTransactions) {

            ResponseTradeGoodsPacket response = new ResponseTradeGoodsPacket();

            // error code = 1 means do nothing
            response.setErrorCode(1);
            response.setId(packet.getId());
            response.setNum(packet.getNum());
            response.setTradeType(packet.getTradeType());
            response.setKind(packet.getKind());
            response.setPriceType(packet.getPriceType());
            response.setTimeType(packet.getTimeType());
            response.setValue(packet.getPrice());

            packet.reply(response);
            return;

        }

        int timeType = packet.getTimeType();

        if (timeType < 0 || timeType > 2) {
            return;
        }

        double tax = tradeBuilding.getLevelData().getEffect("tradeTax").getValue();

        int fee = (int) ((double) packet.getPrice() * tax);
        int spare;

        switch (timeType) {

            // 12 hours
            case 0:
                spare = 12 * 60 * 60;
                break;

            case 1:
                fee = fee + (int) ((double) fee * 0.2);
                spare = 24 * 60 * 60;
                break;

            default:
                fee = fee + (int) ((double) fee * 0.3);
                spare = 48 * 60 * 60;
                break;

        }

        if (fee <= 0) {
            fee = 1;
        }
        boolean isMp = packet.getPriceType() == 1;
        TradeType tradeType = packet.getTradeType() == 1 ? TradeType.ITEM : TradeType.SHIP;

        UserResources userResources = user.getResources();

        if (isMp) {

            if (!(userResources.getMallPoints() >= fee)) {
                return;
            }

            userResources.setMallPoints(userResources.getMallPoints() - fee);

        } else {

            if (!(userResources.getGold() >= fee)) {
                return;
            }

            userResources.setGold(userResources.getGold() - fee);

        }

        Trade trade = null;

        switch (tradeType) {

            case SHIP -> {

                UserShips userShips = user.getShips();
                ShipTeamNum shipTeamNum = userShips.getShipTeamNum(packet.getId());

                if (shipTeamNum == null || shipTeamNum.getNum() == 0) {
                    return;
                }
                if (shipTeamNum.getNum() < packet.getNum()) {
                    return;
                }

                shipTeamNum.setNum(shipTeamNum.getNum() - packet.getNum());
                if (shipTeamNum.getNum() <= 0) {
                    userShips.getShips().remove(shipTeamNum);
                }

                ShipModel shipModel = PacketService.getShipModel(shipTeamNum.getShipModelId());

                trade = TradeShip.builder()

                    .tradeId(AutoIncrementService.getInstance().getNextTradeId())
                    .tradeType(TradeType.SHIP)

                    .sellerUserId(user.getUserId())
                    .sellerGuid(user.getGuid())

                    .sellId(shipModel.getBodyId())
                    .shipModelId(shipTeamNum.getShipModelId())

                    .amount(packet.getNum())
                    .price(packet.getPrice())

                    .build();

                break;

            }

            case ITEM -> {

                UserInventory userInventory = user.getInventory();
                Prop prop = userInventory.getProp(packet.getId());

                if (prop == null) {
                    return;
                }
                if (prop.getPropNum() < packet.getNum()) {
                    return;
                }

                TradeType calculatedType = TradeType.ITEM;
                PropData propData = prop.getData();

                if (propData != null) {
                    switch (propData.getType()) {

                        case "blueprintBody":
                        case "blueprintPart":
                            calculatedType = TradeType.BLUEPRINT;
                            break;

                        case "gem":
                            calculatedType = TradeType.GEM;
                            break;

                        case "commander":
                            calculatedType = TradeType.CARD;
                            break;

                    }
                } else if (prop.getPropId() < MAX_COMMANDER_ID) {
                    calculatedType = TradeType.CARD;
                }

                prop.setPropNum(prop.getPropNum() - packet.getNum());
                trade = TradeItem.builder()

                    .tradeId(AutoIncrementService.getInstance().getNextTradeId())
                    .tradeType(calculatedType)

                    .sellerUserId(user.getUserId())
                    .sellerGuid(user.getGuid())

                    .sellId(prop.getPropId())
                    .propId(prop.getPropId())

                    .amount(packet.getNum())
                    .price(packet.getPrice())

                    .build();

                break;

            }

            default -> {
                return;
            }

        }

        trade.setUntil(DateUtil.now(spare));
        trade.setPriceType(isMp ? PriceType.MP : PriceType.GOLD);

        user.save();
        TradeService.getInstance().getTradeCache().save(trade);

        ResponseTradeGoodsPacket response = new ResponseTradeGoodsPacket();

        response.setErrorCode(0);
        response.setId(packet.getId());
        response.setNum(packet.getNum());
        response.setTradeType(packet.getTradeType());
        response.setKind(packet.getKind());
        response.setPriceType((byte) (isMp ? 1 : 0));
        response.setTimeType(packet.getTimeType());
        response.setValue(fee);

        packet.reply(response);

    }

    @PacketProcessor
    public void onExchange(RequestExchangePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        BuildsJson buildsJson = ResourceManager.getBuilds();
        BuildData buildData = buildsJson.getBuild(11);

        UserBuildings buildings = user.getBuildings();
        List<UserBuilding> building = buildings.getBuildings(11);

        if (building.isEmpty()) {
            return;
        }

        int tradeBuildingLevel = building.get(0).getLevelId();

        BuildLevelMeta buildLevelMeta = buildData.getLevel(tradeBuildingLevel);
        BuildEffectMeta buildEffectMeta = buildLevelMeta.getEffect("resourceExchange");

        UserResources resources = user.getResources();

        double exchange = buildEffectMeta.getValue();
        int result = (int) (packet.getValue() * exchange);

        if (packet.getKind() == 0) { // 0: METAL -> HE3

            if (resources.getMetal() >= packet.getValue()) {

                resources.addHe3(result);
                resources.setMetal(resources.getMetal() - packet.getValue());

                ResponseExchangePacket response = ResponseExchangePacket
                    .builder()
                    .gas(result)
                    .metal(-packet.getValue())
                    .build();

                user.save();
                packet.reply(response);

            }

        } else if (packet.getKind() == 1) { // 1: HE3 -> METAL

            if (resources.getHe3() >= packet.getValue()) {

                resources.addMetal(result);
                resources.setHe3(resources.getHe3() - packet.getValue());

                ResponseExchangePacket response = ResponseExchangePacket
                    .builder()
                    .gas(-packet.getValue())
                    .metal(result)
                    .build();

                user.save();
                packet.reply(response);

            }
        }
    }

}

package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.ShipUpgrade;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.database.entity.sub.UserShipUpgrades;
import com.go2super.obj.game.Prop;
import com.go2super.obj.game.ShipBodyInfo;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.upgrade.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.resources.data.meta.UpgradeMeta;
import com.go2super.resources.data.meta.UpgradePartMeta;
import com.go2super.resources.json.ShipBodyJson;
import com.go2super.resources.json.ShipPartJson;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.sql.Date;
import java.time.Instant;
import java.util.*;

public class UpgradeListener implements PacketListener {

    @PacketProcessor
    public void onSpeedShipBodyUpgrade(RequestSpeedShipBodyUpgradePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        UserResources userResources = user.getResources();

        int kind = packet.getKind();
        int feeType = packet.getFeeType();

        if (packet.getSpeedId() < 0 || packet.getSpeedId() > 4) {
            return;
        }

        // Speed Body
        if (kind == 0) {

            int bodyPart = packet.getBodyPartId();

            if (!shipUpgrades.getCurrentBodies().contains(bodyPart)) {
                return;
            }

            if (shipUpgrades.getShipUpgrade() == null) {
                return;
            }

            ShipUpgrade shipUpgrade = shipUpgrades.getShipUpgrade();
            int spare = DateUtil.remains(shipUpgrade.getUntil()).intValue();

            int reduce;
            int price;

            boolean voucher = feeType == 1;
            boolean complete = false;

            switch (packet.getSpeedId()) {

                case 0:

                    reduce = 30 * 60;
                    price = 8;

                    break;

                case 1:

                    reduce = 2 * 60 * 60;
                    price = 32;

                    break;

                case 2:

                    reduce = 8 * 60 * 60;
                    price = 128;

                    break;

                case 3:

                    reduce = 24 * 60 * 60;
                    price = 384;

                    break;

                default:

                    reduce = spare;
                    price = (spare / 225) + 1;

                    if (price <= 0) {
                        price = 1;
                    }

                    break;

            }

            if (spare - reduce <= 0) {
                complete = true;
            }

            boolean paid = false;

            if (voucher) {
                if (user.getResources().getVouchers() >= price) {

                    user.getResources().setVouchers(user.getResources().getVouchers() - price);
                    paid = true;

                }
            } else {
                if (user.getResources().getMallPoints() >= price) {

                    user.getResources().setMallPoints(user.getResources().getMallPoints() - price);
                    paid = true;

                }
            }

            if (paid) {

                long time = shipUpgrade.getUntil().getTime();
                long reduceMillis = (long) reduce * 1000L;
                time -= (reduceMillis) + 1;

                shipUpgrade.setUntil(Date.from(Instant.ofEpochMilli(time)));
                user.getMetrics().add("action:speedup.body.research", 1);

                user.update();
                user.save();

                ResponseSpeedShipBodyUpgradePacket response = new ResponseSpeedShipBodyUpgradePacket();

                response.setBodyPartId(bodyPart);
                response.setSpareTime(complete ? 0 : (spare - reduce));
                response.setSpeedId(packet.getSpeedId());
                response.setCredit(price);

                response.setKind(packet.getKind());
                response.setFeeType((byte) (voucher ? 1 : 0));

                packet.reply(response);

            }

            // Part upgrade speed
        } else if (kind == 1) {

            int bodyPart = packet.getBodyPartId();

            if (!shipUpgrades.getCurrentParts().contains(bodyPart)) {
                return;
            }

            if (shipUpgrades.getPartUpgrade() == null) {
                return;
            }

            ShipUpgrade shipUpgrade = shipUpgrades.getPartUpgrade();
            int spare = DateUtil.remains(shipUpgrade.getUntil()).intValue();

            int reduce;
            int price;

            boolean voucher = feeType == 1;
            boolean complete = false;

            switch (packet.getSpeedId()) {

                case 0:

                    reduce = 30 * 60;
                    price = 8;

                    break;

                case 1:

                    reduce = 2 * 60 * 60;
                    price = 32;

                    break;

                case 2:

                    reduce = 8 * 60 * 60;
                    price = 128;

                    break;

                case 3:

                    reduce = 24 * 60 * 60;
                    price = 384;

                    break;

                default:

                    reduce = spare;
                    price = (spare / 60 / 10) + 1;

                    if (price <= 0) {
                        price = 1;
                    }

                    break;

            }

            if (spare - reduce <= 0) {
                complete = true;
            }

            boolean paid = false;

            if (voucher) {
                if (user.getResources().getVouchers() >= price) {

                    user.getResources().setVouchers(user.getResources().getVouchers() - price);
                    paid = true;

                }
            } else {

                if (user.getResources().getMallPoints() >= price) {

                    user.getResources().setMallPoints(user.getResources().getMallPoints() - price);
                    paid = true;

                }
            }

            if (paid) {

                long time = shipUpgrade.getUntil().getTime();
                long reduceMillis = (long) reduce * 1000L;
                time -= (reduceMillis) + 1;

                shipUpgrade.setUntil(Date.from(Instant.ofEpochMilli(time)));
                user.getMetrics().add("action:speedup.part.research", 1);

                user.update();
                user.save();

                ResponseSpeedShipBodyUpgradePacket response = new ResponseSpeedShipBodyUpgradePacket();

                response.setBodyPartId(bodyPart);
                response.setSpareTime(complete ? 0 : (spare - reduce));
                response.setSpeedId(packet.getSpeedId());
                response.setCredit(price);

                response.setKind(packet.getKind());
                response.setFeeType((byte) (voucher ? 1 : 0));

                packet.reply(response);

            }

        }

    }

    @PacketProcessor
    public void onShipBodyUpgradeInfo(RequestShipBodyUpgradeInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        ResponseShipBodyUpgradeInfoPacket response = new ResponseShipBodyUpgradeInfoPacket();

        if (shipUpgrades.getShipUpgrade() != null) {

            ShipUpgrade currentUpgrade = shipUpgrades.getShipUpgrade();
            ShipBodyInfo shipBodyInfo = ShipBodyInfo.builder()
                .bodyPartId(currentUpgrade.getUpgradeId())
                .needTime(DateUtil.remains(currentUpgrade.getUntil()).intValue())
                .build();

            response.setBodyNum((short) 1);
            response.setBodyId(Collections.singletonList(shipBodyInfo));

        } else {

            response.setBodyNum((short) 0);
            response.setBodyId(Collections.singletonList(ShipBodyInfo.builder()
                    .bodyPartId(-1)
                    .needTime(-1)
                    .build()));

        }

        if (shipUpgrades.getPartUpgrade() != null) {

            ShipUpgrade currentUpgrade = shipUpgrades.getPartUpgrade();
            ShipBodyInfo shipBodyInfo = ShipBodyInfo.builder()
                .bodyPartId(currentUpgrade.getUpgradeId())
                .needTime(DateUtil.remains(currentUpgrade.getUntil()).intValue())
                .build();

            response.setPartNum((short) 1);
            response.setPartId(Collections.singletonList(shipBodyInfo));

        } else {

            response.setPartNum((short) 0);
            response.setPartId(Collections.singletonList(ShipBodyInfo.builder()
                    .bodyPartId(-1)
                    .needTime(-1)
                    .build()));

        }

        response.setIncUpgradePercent(0);
        packet.reply(response);

    }

    @PacketProcessor
    public void onShipUpgrade(RequestShipBodyUpgradePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        UserResources userResources = user.getResources();

        // BotLogger.log(packet);
        // BotLogger.log(kind);

        // Cancel current body upgrade
        if (packet.getKind() == 0 && packet.getCancelFlag() == 1) {

            int bodyPart = packet.getBodyPartId();

            if (!shipUpgrades.getCurrentBodies().contains(bodyPart)) {
                return;
            }

            if (shipUpgrades.getShipUpgrade() == null) {
                return;
            }

            ShipUpgrade shipUpgrade = shipUpgrades.getShipUpgrade();

            if (shipUpgrade.getUpgradeId() == bodyPart) {

                ShipBodyJson shipBodyJson = ResourceManager.getShipBodies();
                BodyLevelMeta bodyLevelMeta = shipBodyJson.getMeta(bodyPart + 1);

                if (bodyLevelMeta == null) {
                    return;
                }

                UpgradeMeta upgradeMeta = bodyLevelMeta.getUpgrade();
                double goldCost = upgradeMeta.getGold();

                shipUpgrades.setShipUpgrade(null);
                userResources.addGold((int) goldCost);

                user.save();

                ResponseShipBodyUpgradePacket response = new ResponseShipBodyUpgradePacket();

                response.setBodyPartId(bodyPart);
                response.setNeedTime(upgradeMeta.getTime());
                response.setKind(packet.getKind());
                response.setCancelFlag((byte) 1);
                response.setReserve((short) 0);
                response.setMoney((int) goldCost);
                response.setMetal(0);
                response.setGas(0);

                packet.reply(response);


            }

            return;

        }

        if (packet.getKind() == 1 && packet.getCancelFlag() == 1) {

            int bodyPart = packet.getBodyPartId();

            if (!shipUpgrades.getCurrentParts().contains(bodyPart)) {
                return;
            }

            if (shipUpgrades.getPartUpgrade() == null) {
                return;
            }

            ShipUpgrade shipUpgrade = shipUpgrades.getPartUpgrade();

            if (shipUpgrade.getUpgradeId() == bodyPart) {

                ShipPartJson shipPartJson = ResourceManager.getShipParts();
                PartLevelMeta levelMeta = shipPartJson.getMeta(bodyPart + 1);

                if (levelMeta == null) {
                    return;
                }

                UpgradePartMeta upgradeMeta = levelMeta.getUpgrade();
                double goldCost = upgradeMeta.getGold();

                shipUpgrades.setPartUpgrade(null);
                userResources.addGold((int) goldCost);

                user.save();

                ResponseShipBodyUpgradePacket response = new ResponseShipBodyUpgradePacket();

                response.setBodyPartId(bodyPart);
                response.setNeedTime(upgradeMeta.getTime());
                response.setKind(packet.getKind());
                response.setCancelFlag((byte) 1);
                response.setReserve((short) 0);
                response.setMoney((int) goldCost);
                response.setMetal(0);
                response.setGas(0);

                packet.reply(response);

            }

            return;

        }

        // Upgrade Body
        if (packet.getKind() == 0) {

            int bodyPart = packet.getBodyPartId();

            if (!shipUpgrades.getCurrentBodies().contains(bodyPart)) {
                return;
            }

            if (shipUpgrades.getShipUpgrade() != null) {
                return;
            }

            ShipBodyJson shipBodyJson = ResourceManager.getShipBodies();
            BodyLevelMeta bodyLevelMeta = shipBodyJson.getMeta(bodyPart + 1);

            if (bodyLevelMeta == null) {
                return;
            }

            UpgradeMeta upgradeMeta = bodyLevelMeta.getUpgrade();
            double goldCost = upgradeMeta.getGold();

            if (!(userResources.getGold() >= goldCost)) {
                return;
            }

            if (upgradeMeta.getPropId() != -1) {

                Prop prop = user.getInventory().getProp(upgradeMeta.getPropId());

                Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(prop, 1);
                if (!removedProp.getKey()) {
                    return;
                }

            }

            userResources.setGold(userResources.getGold() - (int) goldCost);
            shipUpgrades.setShipUpgrade(ShipUpgrade.builder()
                .upgradeId(bodyPart)
                .until(DateUtil.now(upgradeMeta.getTime()))
                .build());

            user.save();

            ResponseShipBodyUpgradePacket response = new ResponseShipBodyUpgradePacket();

            response.setBodyPartId(bodyPart);
            response.setNeedTime(upgradeMeta.getTime());
            response.setKind(packet.getKind());
            response.setCancelFlag(packet.getCancelFlag());
            response.setReserve((short) 0);
            response.setMoney((int) goldCost);
            response.setMetal(0);
            response.setGas(0);

            packet.reply(response);
            return;

        }

        // Upgrade Part
        int bodyPart = packet.getBodyPartId();

        if (!shipUpgrades.getCurrentParts().contains(bodyPart)) {
            return;
        }

        if (shipUpgrades.getPartUpgrade() != null) {
            return;
        }

        ShipPartJson shipPartJson = ResourceManager.getShipParts();
        PartLevelMeta partData = shipPartJson.getMeta(bodyPart + 1);

        if (partData == null) {
            return;
        }

        // BotLogger.log(partData);
        UpgradePartMeta upgradeMeta = partData.getUpgrade();
        // BotLogger.log(upgradeMeta);
        double goldCost = upgradeMeta.getGold();

        if (!(userResources.getGold() >= goldCost)) {
            return;
        }

        userResources.setGold(userResources.getGold() - (int) goldCost);
        shipUpgrades.setPartUpgrade(ShipUpgrade.builder()
            .upgradeId(bodyPart)
            .until(DateUtil.now(upgradeMeta.getTime()))
            .build());

        user.save();

        ResponseShipBodyUpgradePacket response = new ResponseShipBodyUpgradePacket();

        response.setBodyPartId(bodyPart);
        response.setNeedTime(upgradeMeta.getTime());
        response.setKind(packet.getKind());
        response.setCancelFlag(packet.getCancelFlag());
        response.setReserve((short) 0);
        response.setMoney((int) goldCost);
        response.setMetal(0);
        response.setGas(0);

        packet.reply(response);

    }

}

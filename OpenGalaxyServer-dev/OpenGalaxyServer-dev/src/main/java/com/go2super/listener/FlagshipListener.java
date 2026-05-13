package com.go2super.listener;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.sub.UserResources;
import com.go2super.database.entity.sub.UserShipUpgrades;
import com.go2super.obj.game.Prop;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.flagship.*;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.FlagshipData;
import com.go2super.resources.data.TeslaData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.FlagshipRequirementMeta;
import com.go2super.resources.data.meta.TeslaRequirementMeta;
import com.go2super.resources.data.meta.UpgradeMeta;
import com.go2super.resources.json.ShipBodyJson;
import com.go2super.service.LoginService;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class FlagshipListener implements PacketListener {

    private final static int TESLA_CREATE_GOLD_REQUIREMENT = 1_000_000;
    @PacketProcessor
    public void onUnionFlagship(RequestUnionFlagshipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        FlagshipData flagshipData = ResourceManager.getFlagships().lookup(packet.getPropsId());
        if (flagshipData == null) {
            return;
        }
        if (user.getResources().getGold() < flagshipData.getMoney()) {
            return;
        }

        // System.out.println("HERE");

        UserInventory inventory = user.getInventory();

        for (FlagshipRequirementMeta meta : flagshipData.getRequired()) {
            boolean hasProp = inventory.hasProp(meta.getPropId(), meta.getAmount(), 0, true);
            // System.out.println("Meta: " + meta + ", hasProp: " + hasProp);
            if (!hasProp) {
                return;
            }
        }

        for (FlagshipRequirementMeta meta : flagshipData.getRequired()) {
            inventory.removeProp(meta.getPropId(), meta.getAmount(), 0, true);
        }

        int reward = flagshipData.getPropId();

        if (reward == -1) {
            List<Integer> randomFlagships = Arrays.asList(876, 877, 878, 879, 881, 882, 883, 4460, 4459, 879);
            reward = randomFlagships.get((int) (Math.random() * randomFlagships.size()));
        }

        inventory.addProp(reward, 1, 0, false);
        user.getResources().setGold(user.getResources().getGold() - flagshipData.getMoney());

        ResponseUnionFlagshipPacket response = new ResponseUnionFlagshipPacket();
        response.setPropsId(reward);

        user.update();
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onUpgradeFlagshipPacket(RequestUpgradeFlagshipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        UserResources userResources = user.getResources();

        // Upgrade Body
        int bodyPart = packet.getShipBodyId();

        if (!shipUpgrades.getCurrentBodies().contains(bodyPart)) {
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
            Pair<Boolean, Boolean> removed = user.getInventory().removeProp(prop, upgradeMeta.getPropAmount());

            if (!removed.getLeft()) {
                return;
            }

            ResponseUsePropsPacket responseUsePropsPacket = ResourcesService.getInstance().genericUseProps(prop.getPropId(), upgradeMeta.getPropAmount(), removed.getLeft() ? 0 : 1, 0);
            packet.reply(responseUsePropsPacket);

        }

        userResources.setGold(userResources.getGold() - (int) goldCost);

        shipUpgrades.getCurrentBodies().remove(Integer.valueOf(bodyPart));
        shipUpgrades.getCurrentBodies().add(bodyLevelMeta.getId());

        user.save();

        ResponseUpgradeFlagshipPacket response = new ResponseUpgradeFlagshipPacket();
        response.setShipBodyId(packet.getShipBodyId());
        packet.reply(response);

    }

    @PacketProcessor
    public void onUpgradeShipProps(RequestUpgradeShipPropsPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        UserShipUpgrades shipUpgrades = user.getShipUpgrades();
        UserResources userResources = user.getResources();

        // Upgrade Body
        int bodyPart = packet.getShipBodyId();

        if (!shipUpgrades.getCurrentBodies().contains(bodyPart)) {
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
            Pair<Boolean, Boolean> removed = user.getInventory().removeProp(prop, upgradeMeta.getPropAmount());

            if (!removed.getLeft()) {
                return;
            }

            ResponseUsePropsPacket responseUsePropsPacket = ResourcesService.getInstance().genericUseProps(prop.getPropId(), upgradeMeta.getPropAmount(), removed.getLeft() ? 0 : 1, 0);
            packet.reply(responseUsePropsPacket);

        }

        userResources.setGold(userResources.getGold() - (int) goldCost);

        shipUpgrades.getCurrentBodies().remove(Integer.valueOf(bodyPart));
        shipUpgrades.getCurrentBodies().add(bodyLevelMeta.getId());

        user.save();

        ResponseUpgradeFlagshipPacket response = new ResponseUpgradeFlagshipPacket();
        response.setShipBodyId(packet.getShipBodyId());
        packet.reply(response);

    }




    @PacketProcessor
    public void createTesla(RequestUnionShipPropsPacket packet) {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        TeslaData teslaData = ResourceManager.getTesla().lookup(packet.getPropsId());
        if (teslaData == null) {
            return;
        }

        if (user.getResources().getGold() < TESLA_CREATE_GOLD_REQUIREMENT) {
            return;
        }
        UserInventory inventory = user.getInventory();

        for (TeslaRequirementMeta meta : teslaData.getRequired()) {
            boolean hasProp = inventory.hasProp(meta.getPropId(), meta.getAmount(), 0);
            if (!hasProp) {
                return;
            }
        }

        for (TeslaRequirementMeta meta : teslaData.getRequired()) {
            boolean removed = inventory.removeProp(meta.getPropId(), meta.getAmount(), 0);
            if (!removed) {
                return;
            }
        }

        int reward = teslaData.getPropId();
        inventory.addProp(reward, 1, 0, false);
        user.getResources().setGold(user.getResources().getGold() - TESLA_CREATE_GOLD_REQUIREMENT);

        ResponseUnionShipPropsPacket response = new ResponseUnionShipPropsPacket();
        response.setPropsId(reward);

        user.update();
        user.save();

        packet.reply(response);
    }

}

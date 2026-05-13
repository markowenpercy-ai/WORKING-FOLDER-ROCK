package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.TechUpgradeInfo;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.props.RequestTimeQueuePacket;
import com.go2super.packet.science.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.ResearchData;
import com.go2super.resources.data.meta.BuildEffectMeta;
import com.go2super.resources.data.meta.ResearchLevelMeta;
import com.go2super.resources.json.ScienceJson;
import com.go2super.service.CommanderService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;

import java.sql.Date;
import java.time.Instant;
import java.util.*;

public class ScienceListener implements PacketListener {

    @PacketProcessor
    public void onUnbind(RequestUnbindCommanderCardPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserResources userResources = user.getResources();
        UserInventory userInventory = user.getInventory();

        PropData propData = CommanderService.getInstance().getCommanderPropData(packet.getPropsId());
        if (propData == null) {
            return;
        }

        if (userResources.getMallPoints() < 100) {
            return;
        }
        if (!userInventory.hasProp(packet.getPropsId(), 1, 0, true)) {
            return;
        }

        userInventory.removeProp(packet.getPropsId(), 1, 0, true);
        userInventory.addProp(packet.getPropsId(), 1, 0, false);

        userResources.setMallPoints(userResources.getMallPoints() - 100);

        user.update();
        user.save();

        ResponseUnbindCommanderCardPacket response = new ResponseUnbindCommanderCardPacket();
        response.setPropsId(packet.getPropsId());

        packet.reply(response);

    }

    @PacketProcessor
    public void onAddPack(RequestAddPackPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserResources userResources = user.getResources();
        UserInventory userInventory = user.getInventory();

        if (userInventory.getMaximumStacks() >= 200) {
            return;
        }

        if (userResources.getGold() < 1000) {
            return;
        }

        userResources.setGold(userResources.getGold() - 1000);
        userInventory.setMaximumStacks(userInventory.getMaximumStacks() + 1);

        user.getMetrics().add("action:buy.slot", 1);
        user.update();
        user.save();

        ResponseAddPackPacket response = new ResponseAddPackPacket();
        response.setPropsPack(userInventory.getMaximumStacks());

        packet.reply(response);

    }

    @PacketProcessor
    public void onCreateTech(RequestCreateTechPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuilding userBuilding = user.getBuildings().getBuilding("build:scienceResearch");
        if (userBuilding == null) {
            return;
        }

        BuildEffectMeta effectMeta = userBuilding.getLevelData().getEffect("scienceBonus");
        double scienceBonus = effectMeta.getValue();

        Corp userCorp = user.getCorp();
        if (userCorp != null) {
            scienceBonus += userCorp.getRBPBonus();
        }

        ScienceJson researchJson = ResourceManager.getInstance().getScience();
        ResearchData researchData = researchJson.getResearchData(packet.getTechId());
        if (researchData == null) {
            return;
        }

        UserTech research = user.getTechs().getTech(packet.getTechId());

        if (!researchData.meetRequirements(user) || user.getTechs().getUpgrade() != null) {
            return;
        }
        if (research != null && researchData.getLevels().size() < research.getLevel() + 1) {
            return;
        }

        int nextLevel = research == null ? 1 : research.getLevel() + 1;
        ResearchLevelMeta levelMeta = researchJson.getResearchLevelMeta(packet.getTechId(), nextLevel);

        if (user.getResources().getGold() < levelMeta.getGold()) {
            return;
        }
        if (levelMeta == null) {
            return;
        }

        double baseTime = levelMeta.getTime() / (1.0 + scienceBonus);

        user.getResources().setGold((long) (user.getResources().getGold() - levelMeta.getGold()));
        user.getTechs().setUpgrade(TechUpgrade.builder()
            .id(researchData.getId())
            .level(levelMeta.getLv())
            .until(DateUtil.now((int) baseTime))
            .build());

        user.update();
        user.save();

        ResponseCreateTechPacket response = ResponseCreateTechPacket.builder()
            .techId(packet.getTechId())
            .needTime((int) baseTime)
            .creditFlag(packet.getCreditFlag())
            .build();

        packet.reply(response);

    }

    @PacketProcessor
    public void onCancelTech(RequestCancelTechPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        UserTechs userTechs = user.getTechs();

        if (userTechs.getUpgrade() == null) {
            return;
        }

        ScienceJson scienceJson = ResourceManager.getScience();
        ResearchData researchData = scienceJson.getResearchData(packet.getTechId());
        ResearchLevelMeta levelMeta = researchData.getLevel(userTechs.getUpgrade().getLevel());

        ResponseCancelTechPacket response = ResponseCancelTechPacket.builder()
            .techId(userTechs.getUpgrade().getId())
            .build();

        user.getResources().addGold((long) levelMeta.getGold());
        userTechs.setUpgrade(null);

        user.update();
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onTimeQueue(RequestTimeQueuePacket packet) {

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        packet.reply(user.getQueuesAsPacket());

    }

    @PacketProcessor
    public void onSpeedTech(RequestSpeedTechPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        UserTechs userTechs = user.getTechs();

        if (userTechs.getUpgrade() == null) {
            return;
        }

        if (userTechs.getUpgrade().getId() != packet.getTechId()) {
            return;
        }

        int price = 0;
        int reduce = 0;

        int spare = DateUtil.remains(userTechs.getUpgrade().getUntil()).intValue();

        boolean complete = false;
        boolean voucher = packet.getKind() == 1;

        switch (packet.getTechSpeedId()) {

            case 0:

                reduce = 30 * 60;
                price = 3;

                break;

            case 1:

                reduce = 2 * 60 * 60;
                price = 12;

                break;

            case 2:

                reduce = 8 * 60 * 60;
                price = 48;

                break;

            case 3:

                reduce = 24 * 60 * 60;
                price = 144;

                break;

            default:

                reduce = spare;
                price = (spare / 600) + 1;

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

            long time = userTechs.getUpgrade().getUntil().getTime();
            long reduceMillis = (long) reduce * 1000L;
            time -= (reduceMillis) + 1;

            userTechs.getUpgrade().setUntil((Date.from(Instant.ofEpochMilli(time))));

            user.getMetrics().add("action:speedup.tech", 1);

            user.update();
            user.save();

            ResponseSpeedTechPacket response = new ResponseSpeedTechPacket();

            response.setTechSpeedId(packet.getTechSpeedId());
            response.setCredit(price);
            response.setTime(complete ? 0 : (spare - reduce));

            packet.reply(response);

        }

    }

    @PacketProcessor
    public void onTechUpgradeInfo(RequestTechUpgradeInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuilding userBuilding = user.getBuildings().getBuilding("build:scienceResearch");
        if (userBuilding == null) {
            return;
        }

        BuildEffectMeta effectMeta = userBuilding.getLevelData().getEffect("scienceBonus");
        double scienceBonus = effectMeta.getValue();

        UserTechs userTechs = user.getTechs();

        ResponseTechUpgradeInfoPacket response = new ResponseTechUpgradeInfoPacket();
        List<TechUpgradeInfo> upgradeInfos = response.getTechUpgradeInfoList();

        if (userTechs.getUpgrade() != null) {

            TechUpgrade techUpgrade = userTechs.getUpgrade();

            upgradeInfos.add(new TechUpgradeInfo(DateUtil.remains(techUpgrade.getUntil()).intValue(), techUpgrade.getId(), techUpgrade.getLevel()));
            response.setDataLen((short) 1);

        }

        response.setIncTechPercent((short) (scienceBonus * 100));
        packet.reply(response);

    }

}

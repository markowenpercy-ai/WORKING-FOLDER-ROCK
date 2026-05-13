package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.game.ScenarioBound;
import com.go2super.obj.game.ScenarioInvalid;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.construction.*;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.FortificationData;
import com.go2super.resources.data.meta.BuildIfMeta;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.resources.data.meta.FortificationLevelMeta;
import com.go2super.resources.json.BuildsJson;
import com.go2super.service.CorpService;
import com.go2super.service.GalaxyService;
import com.go2super.service.LoginService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;

import java.sql.Date;
import java.time.Instant;
import java.util.*;

public class BuildListener implements PacketListener {

    private static final ScenarioBound SPACE_BOUND = ScenarioBound.builder()
        .maximumX(24).maximumY(24)
        .minimumX(0).minimumY(0)
        .build();

    private static final ScenarioInvalid SPACE_INVALID = ScenarioInvalid.builder()
        .add(0, 0).add(0, 2).add(0, 1).add(1, 0).add(2, 0).add(1, 1)
        .add(22, 0).add(23, 0).add(24, 0).add(24, 2).add(24, 1).add(23, 1)
        .add(24, 22).add(24, 23).add(24, 24).add(22, 24).add(23, 24).add(23, 23)
        .add(0, 22).add(0, 23).add(0, 24).add(2, 24).add(1, 24).add(1, 23);

    @PacketProcessor
    public void onBuildMove(RequestMoveBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getIndexId() < 0) {
            return;
        }

        UserBuilding building = user.getBuildings().getBuilding(packet.getIndexId());
        if (building == null) {
            return;
        }
        if (isInvalid(building.getData(), packet.getPosX().getValue(), packet.getPosY().getValue())) {
            return;
        }

        building.setX(packet.getPosX().getValue());
        building.setY(packet.getPosY().getValue());

        user.save();

        ResponseMoveBuildPacket response = new ResponseMoveBuildPacket();

        response.setIndexId(packet.getIndexId());
        response.setPosX(packet.getPosX());
        response.setPosY(packet.getPosY());

        packet.reply(response);

    }

    @PacketProcessor
    public void onBuild(RequestCreateBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        BuildsJson json = ResourceManager.getBuilds();
        BuildData data = json.getBuild(packet.getBuildingId());

        if (data == null) {
            return;
        }
        if (isInvalid(data, packet.getPosX().getValue(), packet.getPosY().getValue())) {
            return;
        }

        UserTechs techs = user.getTechs();

        BuildLevelMeta level;
        UserBuildings buildings = user.getBuildings();
        UserBuilding building = buildings.getBuilding(packet.getIndexId());

        int slots = 2;

        if (user.getStats().hasBonus(BonusType.CONSTRUCTION_SLOTS)) {
            slots += 3;
        }
        if (buildings.getUpdatingBuildings().size() + 1 > slots) {
            return;
        }

        UserTech qualityTech = techs.getTech("science:quality.materials");
        double decreaseMetal = 0.0, decreaseGold = 0.0, decreaseHe3 = 0.0;

        if (qualityTech != null) {

            decreaseMetal = qualityTech.getEffectValue("decrease.metal.consume");
            decreaseGold = qualityTech.getEffectValue("decrease.gold.consume");
            decreaseHe3 = qualityTech.getEffectValue("decrease.he3.consume");

        }

        int index = -1;
        int time = 0;

        if (building == null) {

            int count = buildings.count(packet.getBuildingId());
            int limit = data.getLimit();

            if (limit == -1) {

                limit = 0;

                for (BuildIfMeta ifMeta : data.getLimitIf()) {
                    switch (ifMeta.getType()) {
                        case "building":
                            List<UserBuilding> builds = buildings.getBuildings(ifMeta.getId());
                            for (UserBuilding build : builds) {
                                if (build.getLevelId() >= ifMeta.getLv()) {
                                    limit += ifMeta.getAdd();
                                    break;
                                }
                            }
                            continue;

                        case "science":
                            if (techs.has(ifMeta.getId(), ifMeta.getLv())) {
                                limit += ifMeta.getAdd();
                            }
                            continue;
                        default:

                    }
                }
            }

            // Defensive structures
            tech:
            if (data.getType().equals("space")) {

                UserTech userTech = techs.getTech("science:utmost.defense.buildup");
                if (userTech == null) {
                    break tech;
                }

                double effect = userTech.getEffectValue("increase.defensive.structures.maximum.value");
                limit += limit * effect;

            }

            if (count + 1 > limit) {
                return;
            }

            level = data.getLevel(0);

            if (level == null || !level.canBuild(user, decreaseMetal, decreaseGold, decreaseHe3)) {
                System.out.println("Unable to upgrade");
                return;
            }

            time = level.getTime();

            UserTech boostTech = techs.getTech("science:construction.boost");
            if (boostTech != null) {
                time = (int) ((double) time / (1 + (boostTech.getEffectValue("increase.construction.speed") * 0.01)));
            }

            level.charge(user, decreaseMetal, decreaseGold, decreaseHe3);

            building = UserBuilding.builder()
                .index(buildings.nextIndexId())
                .buildingId(packet.getBuildingId())
                .levelId(-1)
                .repairing(false)
                .updating(true)
                .untilUpdate(DateUtil.now(time))
                .x(packet.getPosX().getValue())
                .y(packet.getPosY().getValue())
                .build();

            user.getBuildings().getBuildings().add(building);
            index = building.getIndex();

        } else {

            level = data.getLevel(building.getLevelId() + 1);

            if (level == null || !level.canBuild(user, decreaseMetal, decreaseGold, decreaseHe3)) {
                System.out.println("Unable to upgrade");
                return;
            }

            time = level.getTime();

            UserTech boostTech = techs.getTech("science:construction.boost");
            if (boostTech != null) {
                time -= time * (boostTech.getEffectValue("increase.construction.speed") * 0.01);
            }

            level.charge(user, decreaseMetal, decreaseGold, decreaseHe3);

            building.setUpdating(true);
            building.setUntilUpdate(DateUtil.now(time));

            index = building.getIndex();

        }

        ResponseCreateBuildPacket response = ResponseCreateBuildPacket.builder()
            .buildingId(building.getBuildingId())
            .indexId(index)
            .levelId(building.getLevelId())
            .he3((int) Math.floor(level.getGas() * (1 - (decreaseHe3 * 0.01))))
            .metal((int) Math.floor(level.getMetal() * (1 - (decreaseMetal * 0.01))))
            .money((int) Math.floor(level.getGold() * (1 - (decreaseGold * 0.01))))
            .needTime(time)
            .build();

        user.update();
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onSpeedUp(RequestSpeedBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null || packet.getIndexId() < 0) {
            return;
        }

        UserBuildings buildings = user.getBuildings();

        if (buildings.getBuildings().size() <= packet.getIndexId()) {
            return;
        }

        UserBuilding building = buildings.getBuildings().get(packet.getIndexId());

        if (building == null || !building.getUpdating()) {
            return;
        }

        int price = 0;
        int reduce = 0;

        int spare = building.updatingTime().intValue();

        boolean complete = false;
        boolean voucher = packet.getKind() == 1;

        switch (packet.getBuildingSpeedId()) {

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

            long time = building.getUntilUpdate().getTime();
            long reduceMillis = (long) reduce * 1000L;
            time -= (reduceMillis) + 1;

            building.setUntilUpdate(Date.from(Instant.ofEpochMilli(time)));

            user.getMetrics().add("action:speedup.construction", 1);

            user.update();
            user.save();

            ResponseSpeedBuildPacket response = new ResponseSpeedBuildPacket();

            response.setBuildingSpeedId(packet.getBuildingSpeedId());
            response.setCredit(price);
            response.setIndexId(packet.getIndexId());
            response.setTime(complete ? 0 : (spare - reduce));

            packet.reply(response);

        }

    }

    @PacketProcessor
    public void onCancel(RequestCancelBuildPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null || packet.getIndexId() < 0) {
            return;
        }

        UserBuildings buildings = user.getBuildings();
        UserBuilding building = buildings.getBuilding(packet.getIndexId());

        UserTechs techs = user.getTechs();

        if (building == null) {
            return;
        }
        if (building.getUpdating() == null) {
            return;
        }
        if (!building.getUpdating()) {
            return;
        }

        building.setUpdating(false);
        building.setUntilUpdate(null);

        if (building.getLevelId() == -1) {
            buildings.getBuildings().remove(building);
        }

        UserTech qualityTech = techs.getTech("science:quality.materials");
        double decreaseMetal = 0.0, decreaseGold = 0.0, decreaseHe3 = 0.0;

        if (qualityTech != null) {

            decreaseMetal = qualityTech.getEffectValue("decrease.metal.consume");
            decreaseGold = qualityTech.getEffectValue("decrease.gold.consume");
            decreaseHe3 = qualityTech.getEffectValue("decrease.he3.consume");

        }

        BuildLevelMeta data = building.getData().getLevel(building.getLevelId() + 1);
        UserResources resources = user.getResources();

        int he3 = (int) (Math.floor(data.getGas() * (1 - (decreaseHe3 * 0.01))) * 0.5);
        int metal = (int) (Math.floor(data.getMetal() * (1 - (decreaseMetal * 0.01))) * 0.5);
        int gold = (int) (Math.floor(data.getGold() * (1 - (decreaseGold * 0.01))) * 0.5);

        resources.addHe3(he3);
        resources.addMetal(metal);
        resources.addGold(gold);

        ResponseCancelBuildPacket response = ResponseCancelBuildPacket.builder()
            .indexId(packet.getIndexId())
            .gas(he3)
            .money(gold)
            .metal(metal)
            .status((byte) 1)
            .build();

        user.update();
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onConsortiaBuilding(RequestConsortiaBuildingPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getIndexId() < 0) {
            return;
        }

        Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(packet.getGalaxyId()));
        if (planet == null || planet.getType() != PlanetType.RESOURCES_PLANET) {
            return;
        }

        ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
        if (resourcePlanet == null) ;

        Corp userCorp = user.getCorp();
        if (userCorp == null || userCorp.getMembers().getLeader().getGuid() != user.getGuid()) {
            return;
        }

        Optional<Corp> planetCorp = resourcePlanet.getCorp();
        if (!planetCorp.isPresent() || planetCorp.get().getCorpId() != userCorp.getCorpId()) {
            return;
        }

        RBPBuildings rbpBuildings = resourcePlanet.getBuildings();
        if (rbpBuildings == null || rbpBuildings.getBuildings().isEmpty()) {
            return;
        }

        RBPBuilding rbpBuilding = rbpBuildings.getBuilding(packet.getIndexId());
        if (rbpBuilding == null) {
            return;
        }

        FortificationData fortificationData = rbpBuilding.getData();
        if (fortificationData == null) {
            return;
        }

        FortificationLevelMeta fortificationLevelMeta = fortificationData.getLevel(rbpBuilding.getLevelId());
        FortificationLevelMeta nextFortificationLevelMeta = fortificationData.getLevel(rbpBuilding.getLevelId() + 1);
        if (fortificationLevelMeta == null || nextFortificationLevelMeta == null) {
            return;
        }

        Optional<Integer> optionalWealthRequired = nextFortificationLevelMeta.getWealthRequired();
        if (optionalWealthRequired.isEmpty()) {
            return;
        }

        int wealthRequired = optionalWealthRequired.get();
        if (userCorp.getWealth() < wealthRequired) {
            return;
        }

        rbpBuilding.setLevelId(rbpBuilding.getLevelId() + 1);
        userCorp.setWealth(userCorp.getWealth() - wealthRequired);

        CorpService.getInstance().getCorpCache().save(userCorp);
        GalaxyService.getInstance().getPlanetCache().save(resourcePlanet);

        ResponseConsortiaBuildingPacket response = new ResponseConsortiaBuildingPacket();

        response.setGalaxyId(resourcePlanet.getPosition().galaxyId());
        response.setIndexId(packet.getIndexId());
        response.setWealth(userCorp.getWealth());

        packet.reply(response);

    }

    public boolean isInvalid(BuildData buildData, int x, int y) {

        if (buildData.getType().equals("space")) {
            if (x > SPACE_BOUND.getMaximumX() || x < SPACE_BOUND.getMinimumX() ||
                y > SPACE_BOUND.getMaximumY() || y < SPACE_BOUND.getMinimumY()) {
                return true;
            }
            return SPACE_INVALID.isInvalid(x, y);
        } else {

            // todo test terrain build bounds

        }

        return false;

    }

}

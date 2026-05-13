package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.TeamModelSlot;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.CreateShipInfo;
import com.go2super.obj.game.ShipModelInfo;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.ship.RequestCreateTeamModelPacket;
import com.go2super.packet.ship.RequestSpeedShipPacket;
import com.go2super.packet.ship.ResponseSpeedShipPacket;
import com.go2super.packet.shipmodel.*;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ShipFactoryListener implements PacketListener {

    public static final int MAX_SHIPS = 5_000_000;

    private static final int MAX_SHIP_PER_SLOT = 100_000;

    @PacketProcessor
    public void onCreate(RequestCreateShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getNum() <= 0) {
            return;
        }

        int currentShips = user.totalShips();
        if (currentShips + packet.getNum() >= MAX_SHIPS) {
            return;
        }

        int shipNum = Math.min(packet.getNum(), MAX_SHIP_PER_SLOT);

        // System.out.println(packet);

        UserTechs techs = user.getTechs();
        UserBuilding building = user.getBuildings().getBuilding("build:shipFactory");
        if (building == null) {
            return;
        }

        // System.out.println("A1");

        ShipModel shipModel = PacketService.getShipModel(packet.getShipModelId());

        // System.out.println(shipModel);
        if (shipModel == null) {
            return;
        }

        if (shipModel.getGuid() != -1) {
            if (shipModel.getGuid() != user.getGuid() || shipModel.isDeleted()) {
                return;
            }
        }

        // System.out.println("A2");

        UserShips ships = user.getShips();
        UserResources resources = user.getResources();

        int slots = 0;

        double shipQueueSlots = building.getLevelData().getEffect("shipQueueSlots").getValue();
        double shipBuildBonus = building.getLevelData().getEffect("shipBuildBonus").getValue();

        Corp userCorp = user.getCorp();
        if (userCorp != null) {
            shipBuildBonus += userCorp.getRBPBonus();
        }

        UserTech boostTech = techs.getTech("science:ship.building.boost");
        if (boostTech != null) {
            shipBuildBonus += boostTech.getEffectValue("increase.shipbuilding.speed") * 0.01;
        }

        shipBuildBonus += user.getStats().getShipBuildingBuff();

        slots += (int) shipQueueSlots;

        UserTech syncTech = techs.getTech("science:sync.shipbuilding");
        if (syncTech != null) {
            slots += syncTech.getEffectValue("increase.shipbuilding.queue");
        }
        if (ships.getFactory().size() + 1 > slots) {
            return;
        }

        UserTech qualityTech = techs.getTech("science:ship.building.logistics");
        double decreaseMetal = 0.0, decreaseGold = 0.0, decreaseHe3 = 0.0;

        if (qualityTech != null) {

            decreaseMetal = qualityTech.getEffectValue("decrease.ship.metal.consume");
            decreaseGold = qualityTech.getEffectValue("decrease.ship.gold.consume");
            decreaseHe3 = qualityTech.getEffectValue("decrease.ship.he3.consume");

        }

        double unitGas = (int) Math.floor(shipModel.getHe3BuildCost() * (1 - (decreaseHe3 * 0.01)));
        double unitMetal = (int) Math.floor(shipModel.getMetalBuildCost() * (1 - (decreaseMetal * 0.01)));
        double unitGold = (int) Math.floor(shipModel.getGoldBuildCost() * (1 - (decreaseGold * 0.01)));

        double gas = unitGas * shipNum;
        double metal = unitMetal * shipNum;
        double gold = unitGold * shipNum;

        // System.out.println("A3");

        if (resources.getGold() < gold || resources.getMetal() < metal || resources.getHe3() < gas) {
            return;
        }

        // System.out.println("A4");

        resources.setHe3(resources.getHe3() - (long) gas);
        resources.setMetal(resources.getMetal() - (long) metal);
        resources.setGold(resources.getGold() - (long) gold);

        boolean fastBuild = PacketService.getInstance().getFastShipBuilding();
        double buildTime = fastBuild ? 0 : (double) (shipModel.getBuildTime()) / (1.0 + shipBuildBonus);
        if(!fastBuild && buildTime < 1)
        {
            buildTime = 1;
        }
        int fixedBuildTime = (int) buildTime;
        double needTime = fixedBuildTime * shipNum;

        if (!ships.fabricate(packet.getShipModelId(), shipNum, fixedBuildTime)) {
            return;
        }

        ResponseCreateShipPacket response = new ResponseCreateShipPacket();

        response.setGas((int) gas);
        response.setMetal((int) metal);
        response.setMoney((int) gold);
        response.setNeedTime((int) needTime);
        response.setNum(shipNum);
        response.setShipModelId(packet.getShipModelId());

        user.save();
        packet.reply(response);

        // System.out.println("Response: " + response);

    }

    @PacketProcessor
    public void onDesignShip(RequestCreateShipModelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null || packet.getPartNum() <= 0 || packet.getPartNum() > 50) {
            return;
        }

        List<ShipModel> models = PacketService.getInstance().getShipModelCache().findAllByGuidAndDeleted(packet.getGuid(), false);
        if (models.size() >= 29) {
            return;
        }

        List<Integer> parts = new ArrayList<>();
        for (int i = 0; i < packet.getPartNum(); ++i) {
            parts.add(packet.getParts().get(i));
        }
        if (!ShipService.getInstance().validateShipModel(user, packet.getBodyId(), parts)) {
            return;
        }
        String s = packet.getShipName().noSpaces();
        String regex = "^[a-zA-Z0-9\\s\\-]+$";
        if (!s.matches(regex)) {
            return;
        }

        ShipModel model = ShipModel.builder()
                .shipModelId(AutoIncrementService.getInstance().getNextShipModelId())
                .bodyId(packet.getBodyId())
                .deleted(false)
                .guid(packet.getGuid())
                .name(s)
                .parts(parts)
                .build();

        ResponseCreateShipModelPacket response = new ResponseCreateShipModelPacket();

        response.setShipModelId(model.getShipModelId());
        response.setBodyId((short) model.getBodyId());
        response.getShipName().setValue(model.getName());
        response.setParts(packet.getParts());
        response.setPartNum(packet.getPartNum());
        response.setNeedMoney(0);

        user.getMetrics().add("action:design", 1);
        user.update();
        user.save();

        PacketService.getInstance().getShipModelCache().save(model);
        packet.reply(response);

    }

    @PacketProcessor
    public void onCancelShip(RequestCancelShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        if (packet.getIndexId() < 0 || packet.getIndexId() > 4) {
            return;
        }

        UserShips ships = user.getShips();
        List<FactoryShip> factory = ships.getFactory();

        if (factory.size() <= packet.getIndexId()) {
            return;
        }

        FactoryShip factoryShip = factory.get(packet.getIndexId());
        factory.remove(factoryShip);

        ShipModel shipModel = PacketService.getShipModel(factoryShip.getShipModelId());

        double goldRefund = (shipModel.getGoldBuildCost() * factoryShip.getNum()) * 0.1;
        double he3Refund = (shipModel.getHe3BuildCost() * factoryShip.getNum()) * 0.1;
        double metalRefund = (shipModel.getMetalBuildCost() * factoryShip.getNum()) * 0.1;

        user.getResources().addGold((long) goldRefund);
        user.getResources().addHe3((long) he3Refund);
        user.getResources().addMetal((long) metalRefund);

        user.update();
        user.save();

        ResponseCancelShipPacket response = new ResponseCancelShipPacket();

        response.setIndexId(packet.getIndexId());
        response.setNum(factoryShip.getNum());
        response.setStatus(1);

        packet.reply(response);
        packet.reply(ResourcesService.getInstance().getPlayerResourcePacket(user));

    }

    @PacketProcessor
    public void onDelete(RequestDeleteShipModelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }

        ShipModel model = PacketService.getShipModel(packet.getShipModelId());

        if (model == null || model.getGuid() != packet.getGuid()) {
            return;
        }

        model.setDeleted(true);
        PacketService.getInstance().getShipModelCache().save(model);

    }

    @PacketProcessor
    public void onFactory(RequestCreateShipInfoPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserBuilding building = user.getBuildings().getBuilding("build:shipFactory");
        UserTechs techs = user.getTechs();
        UserShips ships = user.getShips();
        if (building == null) {
            return;
        }

        double shipBuildBonus = building.getLevelData().getEffect("shipBuildBonus").getValue();

        Corp userCorp = user.getCorp();
        if (userCorp != null) {
            shipBuildBonus += userCorp.getRBPBonus();
        }

        UserTech boostTech = techs.getTech("science:ship.building.boost");
        if (boostTech != null) {
            shipBuildBonus += boostTech.getEffectValue("increase.shipbuilding.speed") * 0.01;
        }

        shipBuildBonus += user.getStats().getShipBuildingBuff();

        ResponseCreateShipInfoPacket response = new ResponseCreateShipInfoPacket();

        response.setMaxCreateShipNum(ShipFactoryListener.MAX_SHIPS - user.totalShips());
        response.setIncShipPercent((short) (shipBuildBonus * 100));
        response.setDataLen((short) ships.getFactory().size());

        List<CreateShipInfo> factory = user.getShips().getFactoryAsBuffer();
        CreateShipInfo reference = new CreateShipInfo();

        for (FactoryShip ship : ships.getFactory()) {
            factory.add(ship.packet());
        }

        while (factory.size() < 10) {
            factory.add(reference.trash());
        }

        response.setCreateShipList(factory);
        packet.reply(response);

    }

    @PacketProcessor
    public void onSpeedShip(RequestSpeedShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserShips ships = user.getShips();
        if (ships.getFactory().size() <= packet.getIndexId()) {
            return;
        }

        UserResources resources = user.getResources();
        if (resources.getMallPoints() < 8) {
            return;
        }

        FactoryShip factoryShip = ships.getFactory().get(packet.getIndexId());
        if (factoryShip == null) {
            return;
        }

        int remains = DateUtil.remains(factoryShip.getUntil()).intValue();
        double newRemains = (double) remains / 1.1;

        double buildTime = factoryShip.getBuildTime() / 1.1;
        double needTime = newRemains + (buildTime * (factoryShip.getNum() - 1));

        if (needTime - factoryShip.getNum() <= 0) {
            return;
        }

        factoryShip.setIncSpeed(factoryShip.getIncSpeed() + 10);
        factoryShip.setUntil(DateUtil.now((int) newRemains));
        factoryShip.setBuildTime(buildTime);

        resources.setMallPoints(resources.getMallPoints() - 8);

        ResponseSpeedShipPacket response = ResponseSpeedShipPacket.builder()
                .errorCode(0)
                .spareTime((int) needTime)
                .indexSpareTime(packet.getIndexId())
                .build();

        user.getMetrics().add("action:speedup.shipbuilding", 1);
        user.update();
        user.save();

        packet.reply(response);

    }

    @PacketProcessor
    public void onAddShipModelDel(RequestAddShipModelDelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        ShipModel shipModel = PacketService.getShipModel(packet.getShipModelId());
        if (shipModel == null) {
            return;
        }

        ResponseShipModelInfoDelPacket response = new ResponseShipModelInfoDelPacket();

        response.setDataLen(UnsignedShort.of(1));
        response.setShipModelInfoList(new ArrayList<>());

        response.getShipModelInfoList().add(
                ShipModelInfo.of(
                        shipModel.getName(),
                        shipModel.partNum(),
                        shipModel.getShipModelId() == 0 ? 1 : 0,
                        shipModel.getBodyId(),
                        shipModel.partArray(),
                        shipModel.getShipModelId())
        );

        packet.reply(response);


    }

    @PacketProcessor
    public void onCreateTeamModel(RequestCreateTeamModelPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        TeamModelSlot teamModelSlot = TeamModelService.getInstance().getTeamModelsRepository().findByGuidAndIndexId(packet.getGuid(), packet.getIndexId());

        if (packet.getDelFlag() == 0) {

            if (teamModelSlot == null) {

                TeamModelSlot newTeamModelSlot = TeamModelSlot.builder()
                        .guid(packet.getGuid())
                        .indexId(packet.getIndexId())
                        .teamModel(packet.getTeamModel())
                        .build();

                newTeamModelSlot.save();

            } else {

                teamModelSlot.setTeamModel(packet.getTeamModel());
                teamModelSlot.save();

            }


        } else if (packet.getDelFlag() == 1) {

            if (teamModelSlot != null) {
                TeamModelService.getInstance().getTeamModelsRepository().delete(teamModelSlot);
            }

        }

    }

}

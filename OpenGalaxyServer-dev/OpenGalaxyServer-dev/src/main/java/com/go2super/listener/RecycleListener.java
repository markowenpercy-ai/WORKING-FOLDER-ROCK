package com.go2super.listener;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.recycle.RequestDestroyShipPacket;
import com.go2super.packet.recycle.ResponseDestroyShipPacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

public class RecycleListener implements PacketListener {

    @PacketProcessor
    public void onDestroyShip(RequestDestroyShipPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null || packet.getDataLen() <= 0 || packet.getDataLen() > 100) {
            return;
        }

        if (packet.getShipNums().getTeamNums().isEmpty() || packet.getShipNums().getTeamNums().size() != 100) {
            return;
        }

        UserBuilding userBuilding = user.getBuildings().getBuilding("build:shipRecycle");
        UserShips userShips = user.getShips();

        if (userBuilding == null || userShips == null || userBuilding.getLevelId() == -1) {
            return;
        }

        double scrapValue = userBuilding.getLevelData().getEffect("shipScrap").getValue();

        long gold = 0;
        long metal = 0;
        long he3 = 0;

        int amount = 0;

        for (int i = 0; i < packet.getDataLen(); i++) {

            ShipTeamNum teamNum = packet.getShipNums().getTeamNums().get(i);

            if (teamNum.getShipModelId() <= -1 || teamNum.getNum() <= 0) {
                continue;
            }

            if (!userShips.removeShip(teamNum.getShipModelId(), teamNum.getNum())) {
                return;
            }

            ShipModel shipModel = PacketService.getShipModel(teamNum.getShipModelId());

            gold += (long) shipModel.getGoldBuildCost() * teamNum.getNum();
            metal += (long) shipModel.getMetalBuildCost() * teamNum.getNum();
            he3 += (long) shipModel.getHe3BuildCost() * teamNum.getNum();

            amount += teamNum.getNum();

        }

        gold = (int) ((double) gold * scrapValue);
        metal = (int) ((double) metal * scrapValue);
        he3 = (int) ((double) he3 * scrapValue);

        user.getResources().addGold(gold);
        user.getResources().addMetal(metal);
        user.getResources().addHe3(he3);

        user.getMetrics().add("action:recycle", amount);
        user.update();
        user.save();

        ResponseDestroyShipPacket response = new ResponseDestroyShipPacket();

        response.setMoney(Long.valueOf(gold).intValue());
        response.setMetal(Long.valueOf(metal).intValue());
        response.setGas(Long.valueOf(he3).intValue());

        packet.reply(response);

    }

}

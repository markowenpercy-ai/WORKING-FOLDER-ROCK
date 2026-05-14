package com.go2super.listener;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.BruiseShipInfo;
import com.go2super.obj.game.ShipModelInfo;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.ship.*;
import com.go2super.packet.shipmodel.ResponseShipModelInfoPacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;
import com.go2super.socket.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.go2super.listener.ShipFactoryListener.MAX_SHIPS;

public class ShipRepairListener implements PacketListener {

    private final static int CANCEL_REPAIR = 1;
    private final static int REPAIR = 0;

    @PacketProcessor
    public void onCreate(RequestBruiseShipInfoPacket packet) throws BadGuidException {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserShips userShips = user.getShips();

        FactoryShip repairing = userShips.getRepairFactory();
        List<BruiseShip> bruiseShipList = userShips.getRepair();

        if (bruiseShipList == null) {
            bruiseShipList = new ArrayList<>();
        }
        List<BruiseShipInfo> infos = new ArrayList<>();

        for (BruiseShip bruiseShip : bruiseShipList) {
            infos.add(new BruiseShipInfo(bruiseShip.getShipModelId(), bruiseShip.getNum()));
        }
        ResponseBruiseShipInfoPacket response = new ResponseBruiseShipInfoPacket();
        if (repairing == null) {
            response.setShipModelId(-1);
            response.setNum(0);
            response.setNeedTime(0);
        } else {
            response.setShipModelId(repairing.getShipModelId());
            response.setNum(repairing.getNum());
            response.setNeedTime(repairing.needTime() / repairing.getNum());
        }

        response.setDeadShipData(infos);
        response.setDataLen(infos.size());

        packet.reply(response);
    }

    private void sendReliveError(Packet packet, int errorCode) {
        var resp = new ResponseBruiseShipRelivePacket();
        resp.setErrorCode(errorCode);
        resp.setType(0);
        packet.reply(resp);
    }

    @PacketProcessor
    public void onRelive(RequestBruiseShipRelivePacket packet) throws BadGuidException {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (packet.getKind() != CANCEL_REPAIR && packet.getKind() != REPAIR) {
            return;
        }

        UserShips userShips = user.getShips();

        if (packet.getKind() == CANCEL_REPAIR) {
            if (userShips.cancelRepair(packet.getShipModelId())) {
                var resp = new ResponseBruiseShipRelivePacket();
                resp.setShipModelId(packet.getShipModelId());
                resp.setNum(0);
                resp.setNeedTime(0);
                resp.setType(0);
                resp.setErrorCode(0);
                user.save();
                packet.reply(resp);
            }
            return;
        }

        // REPAIR path — validate bruised ships exist and have enough count
        BruiseShip b = userShips.getRepairShip(packet.getShipModelId());
        if (b == null || b.getNum() <= 0 || packet.getNum() <= 0 || packet.getNum() > b.getNum()) {
            sendReliveError(packet, 1);
            return;
        }

        int currentShips = user.totalShips();
        if (currentShips + packet.getNum() >= MAX_SHIPS) {
            sendReliveError(packet, 1);
            return;
        }

        ShipModel shipModel = PacketService.getShipModel(packet.getShipModelId());
        if (shipModel == null) {
            return;
        }

        FactoryShip factory = userShips.getRepairFactory();
        double repairBoost = 0.00d;

        UserTechs techs = user.getTechs();
        UserTech boostTech = techs.getTech("science:repair.techology");
        if (boostTech != null) {
            repairBoost += boostTech.getEffectValue("ship.repair.rate") * 0.01;
        }

        repairBoost += user.getStats().getRepairBuff();

        double repairTime = (shipModel.getBuildTime()) / (1.0 + repairBoost);
        int fixedBuildTime = (int) repairTime;
        double needTime = fixedBuildTime * packet.getNum();

        if (factory == null) {
            userShips.setRepairFactory(new FactoryShip(packet.getShipModelId(), packet.getNum(), needTime));
        } else if (factory.getShipModelId() == packet.getShipModelId()) {
            int newNum = factory.getNum() + packet.getNum();
            double totalTime = (factory.getBuildTime() * factory.getNum()) + (fixedBuildTime * packet.getNum());
            double avgTime = totalTime / newNum;
            factory.setNum(newNum);
            factory.setBuildTime(avgTime);
            long newUntilMillis = DateUtil.millis() + (long) (avgTime * 1000 * newNum);
            factory.setUntil(new Date(newUntilMillis));
        } else {
            // Different model already repairing — can't queue, return current queue info
            var resp = new ResponseBruiseShipRelivePacket();
            resp.setShipModelId(packet.getShipModelId());
            resp.setNum(b.getNum());
            resp.setNeedTime(fixedBuildTime);
            resp.setErrorCode(1);
            resp.setType(0);
            packet.reply(resp);
            return;
        }

        int remaining = b.getNum() - packet.getNum();
        if (remaining <= 0) {
            userShips.getRepair().remove(b);
        } else {
            b.setNum(remaining);
        }

        var resp = new ResponseBruiseShipRelivePacket();
        resp.setShipModelId(packet.getShipModelId());
        resp.setNum(b.getNum());
        resp.setNeedTime(fixedBuildTime);
        resp.setErrorCode(0);
        resp.setType(0);

        user.save();
        packet.reply(resp);
    }

    @PacketProcessor
    public void onDelete(RequestBruiseShipDeletePacket packet) throws BadGuidException {
        LoginService.validate(packet, packet.getGuid());
        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }
        UserShips userShips = user.getShips();
        var response = new ResponseBruiseShipDeletePacket();
        boolean deleted = userShips.deleteRepairShip(packet.getShipModelId());
        response.setShipModelId(deleted ? packet.getShipModelId() : -1);
        packet.reply(response);
    }

    @PacketProcessor
    public void speedUpRepair(RequestSpeedBruiseShipPacket packet) throws BadGuidException {
        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }
        if (user.getStats().getSp() < 10) {
            var response = new ResponseSpeedBruiseShipPacket();
            response.setShipModelId(packet.getShipModelId());
            response.setErrorCode(1);
            packet.reply(response);
            return;
        }
        FactoryShip repairFactory = user.getShips().getRepairFactory();
        if (repairFactory == null || repairFactory.getShipModelId() != packet.getShipModelId()) {
            var response = new ResponseSpeedBruiseShipPacket();
            response.setShipModelId(packet.getShipModelId());
            response.setErrorCode(1);
            packet.reply(response);
            return;
        }

        int remains = DateUtil.remains(repairFactory.getUntil()).intValue();
        double buildTime = repairFactory.getBuildTime();
        int numShips = repairFactory.getNum();
        double totalRemainingTime = remains + (buildTime * (numShips - 1) * 1000);
        double newTotalTime = totalRemainingTime / 1.1;
        double newBuildTime = newTotalTime / (numShips * 1000);
        long newUntilMillis = DateUtil.millis() + (long) newBuildTime;
        repairFactory.setIncSpeed(repairFactory.getIncSpeed() + 10);
        repairFactory.setUntil(new Date(newUntilMillis));
        repairFactory.setBuildTime(newBuildTime);
        user.getStats().setSp(user.getStats().getSp() - 10);

        var response = new ResponseSpeedBruiseShipPacket();
        response.setShipModelId(packet.getShipModelId());
        response.setErrorCode(0);
        response.setShipModelId(repairFactory.getShipModelId());
        response.setSpareTime((int) newTotalTime / repairFactory.getNum());

        user.update();
        user.save();
        packet.reply(response);
    }

}

package com.go2super.service.jobs.user;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.FactoryShip;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.ship.ResponseShipCreatingCompletePacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ShipConstructionJob implements OfflineJob {

    private long lastExecution = 0L;

    @Override
    public void setup() {

    }

    @Override
    public void run() {
        if (DateUtil.millis() - lastExecution < getInterval()) {
            return;
        }
        lastExecution = DateUtil.millis();

        CopyOnWriteArrayList<Integer> toUpdate = new CopyOnWriteArrayList<>();

        List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getGameUsers();
        for (LoggedGameUser loggedUser : loggedGameUsers) {
            User user = loggedUser.getUpdatedUser();

            UserShips ships = user.getShips();
            if (ships == null) {
                continue;
            }

            List<FactoryShip> factory = ships.getFactory();
            if (factory == null || factory.isEmpty()) {
                continue;
            }

            boolean save = false;
            List<FactoryShip> toDelete = new ArrayList<>();
            for (int i = 0; i < factory.size(); ++i) {
                FactoryShip factoryShip = factory.get(i);

                if (factoryShip.getUntil() == null) {
                    save = true;
                    toDelete.add(factoryShip);
                    continue;
                }
                ShipModel shipModel = PacketService.getShipModel(factoryShip.getShipModelId());

                long timeRemain = DateUtil.remainsMillis(factoryShip.getUntil());
                while (factoryShip.getUntil() != null && timeRemain <= 0) {
                    int numRemain = factoryShip.getNum() - 1;
                    factoryShip.setNum(numRemain);

                    if (numRemain == 0) {
                        factoryShip.setUntil(null);
                        toDelete.add(factoryShip);
                    } else {
                        int next = (int) (factoryShip.getBuildTime() * 1000 + timeRemain);
                        Date until = DateUtil.nowMillis(next);
                        factoryShip.setUntil(until);
                        timeRemain = DateUtil.remainsMillis(until);
                    }

                    ships.addShip(factoryShip.getShipModelId(), 1);

                    ResponseShipCreatingCompletePacket packet = new ResponseShipCreatingCompletePacket();
                    packet.setIndexId(i);
                    loggedUser.getSmartServer().send(packet);

                    user.getMetrics().add("action:build.ship", 1);

                    if (shipModel != null) {
                        user.getStats().addExp(shipModel.getHe3BuildCost() + shipModel.getMetalBuildCost() + shipModel.getGoldBuildCost());
                    }
                    save = true;
                }
            }

            factory.removeAll(toDelete);

            if (save) {
                user.update();
                user.save();
            }
        }
    }

    @Override
    public long getInterval() {
        return 1L;
    }

}

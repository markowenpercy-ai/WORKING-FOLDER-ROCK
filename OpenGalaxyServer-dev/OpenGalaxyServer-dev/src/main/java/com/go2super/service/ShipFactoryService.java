package com.go2super.service;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.FactoryShip;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.socket.util.DateUtil;
import org.springframework.stereotype.Service;

import java.util.*;

// pretty sure i made this shit worse, cant remember if i reverted, so its staying this way

@Service
public class ShipFactoryService {
    private static ShipFactoryService instance;

    public ShipFactoryService() {
        instance = this;
    }

    public void catchUpUserFactory(User user) {
        UserShips ships = user.getShips();
        if (ships == null) {
            return;
        }

        List<FactoryShip> factory = ships.getFactory();
        if (factory == null || factory.isEmpty()) {
            return;
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

            long timeRemain = DateUtil.remainsMillis(factoryShip.getUntil());
            if (factoryShip.getUntil() != null && timeRemain <= 0) {
                long buildTime = (long)(factoryShip.getBuildTime() * 1000);

                int shipsCompleted = (int)(-timeRemain / buildTime) + 1;
                timeRemain = -timeRemain % buildTime;

                shipsCompleted = Math.min(shipsCompleted, factoryShip.getNum());
                int numRemain = factoryShip.getNum() - shipsCompleted;
                factoryShip.setNum(numRemain);

                if (numRemain == 0) {
                    factoryShip.setUntil(null);
                    toDelete.add(factoryShip);
                } else {
                    int next = (int)(factoryShip.getBuildTime() * 1000 + timeRemain);
                    factoryShip.setUntil(DateUtil.nowMillis(next));
                }

                ships.addShip(factoryShip.getShipModelId(), shipsCompleted);

                user.getMetrics().add("action:build.ship", shipsCompleted);
                save = true;
            }

            factory.removeAll(toDelete);

            if (save) {
                user.update();
                user.save();
            }
        }
    }

    public static ShipFactoryService getInstance() {
        return instance;
    }
}

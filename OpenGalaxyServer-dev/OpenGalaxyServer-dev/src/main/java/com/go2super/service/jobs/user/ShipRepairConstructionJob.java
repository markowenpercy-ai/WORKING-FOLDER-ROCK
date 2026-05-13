package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.FactoryShip;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.service.LoginService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.Date;
import java.util.List;

public class ShipRepairConstructionJob implements OfflineJob {

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

        List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getGameUsers();
        for (LoggedGameUser loggedUser : loggedGameUsers) {
            User user = loggedUser.getUpdatedUser();

            UserShips ships = user.getShips();
            if (ships == null) {
                continue;
            }

            FactoryShip factoryShip = ships.getRepairFactory();
            if (factoryShip == null) {
                continue;
            }

            boolean save = false;

            if (factoryShip.getUntil() == null) {
                long nextTime = DateUtil.millis() + (long) (factoryShip.getBuildTime() * 1000);
                factoryShip.setUntil(new Date(nextTime));
                save = true;
            } else if (DateUtil.remainsMillis(factoryShip.getUntil()) <= 0) {
                ships.addShip(factoryShip.getShipModelId(), 1);
                int remaining = factoryShip.getNum() - 1;
                if (remaining <= 0) {
                    ships.setRepairFactory(null);
                } else {
                    factoryShip.setNum(remaining);
                    long nextTime = DateUtil.millis() + (long) (factoryShip.getBuildTime() * 1000);
                    factoryShip.setUntil(new Date(nextTime));
                }
                save = true;
            }

            if (save) {
                user.update();
                user.save();
            }
        }
    }

    @Override
    public long getInterval() {
        return 1L; // why this shit run so much
    }

}

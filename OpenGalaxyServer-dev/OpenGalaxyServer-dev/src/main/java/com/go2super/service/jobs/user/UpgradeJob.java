package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.ShipUpgrade;
import com.go2super.database.entity.sub.UserShipUpgrades;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.upgrade.ResponseShipBodyUpgradeCompletePacket;
import com.go2super.service.ResourcesService;
import com.go2super.service.jobs.GalaxyUserJob;

public class UpgradeJob implements GalaxyUserJob {

    @Override
    public String getName() {

        return "upgrade-job";
    }

    @Override
    public boolean needUpdate(User user) {

        UserShipUpgrades upgrades = user.getShipUpgrades();
        if (upgrades.getShipUpgrade() == null && upgrades.getPartUpgrade() == null) {
            return false;
        }

        return (upgrades.getShipUpgrade() != null && upgrades.getShipUpgrade().upgradeTime() <= 0) ||
               (upgrades.getPartUpgrade() != null && upgrades.getPartUpgrade().upgradeTime() <= 0);

    }

    @Override
    public boolean run(LoggedGameUser loggedGameUser, User user) {

        UserShipUpgrades upgrades = user.getShipUpgrades();
        if (upgrades.getShipUpgrade() == null && upgrades.getPartUpgrade() == null) {
            return false;
        }

        boolean update = false;

        if (upgrades.getShipUpgrade() != null) {

            ShipUpgrade shipUpgrade = upgrades.getShipUpgrade();

            if (shipUpgrade.upgradeTime() <= 0) {

                update = true;

                upgrades.getCurrentBodies().remove(Integer.valueOf(shipUpgrade.getUpgradeId()));
                upgrades.getCurrentBodies().add(shipUpgrade.getUpgradeId() + 1);
                upgrades.setShipUpgrade(null);

                user.getMetrics().add("action:ship.upgrade", 1);

                ResponseShipBodyUpgradeCompletePacket response = buildCompletePacket(shipUpgrade.getUpgradeId(), 0);
                loggedGameUser.getSmartServer().send(response);

            }

        }

        if (upgrades.getPartUpgrade() != null) {

            ShipUpgrade partUpgrade = upgrades.getPartUpgrade();

            if (partUpgrade.upgradeTime() <= 0) {

                update = true;

                upgrades.getCurrentParts().remove(Integer.valueOf(partUpgrade.getUpgradeId()));
                upgrades.getCurrentParts().add(partUpgrade.getUpgradeId() + 1);
                upgrades.setPartUpgrade(null);

                user.getMetrics().add("action:part.upgrade", 1);

                ResponseShipBodyUpgradeCompletePacket response = buildCompletePacket(partUpgrade.getUpgradeId(), 1);
                loggedGameUser.getSmartServer().send(response);

            }

        }

        if (update) {
            loggedGameUser.getSmartServer().send(ResourcesService.getInstance().getPlayerResourcePacket(user));
        }
        return update;

    }

    public ResponseShipBodyUpgradeCompletePacket buildCompletePacket(int bodyPartId, int type) {

        ResponseShipBodyUpgradeCompletePacket response = new ResponseShipBodyUpgradeCompletePacket();

        response.setBodyPartId(bodyPartId);
        response.setKind((byte) type);

        return response;

    }

}

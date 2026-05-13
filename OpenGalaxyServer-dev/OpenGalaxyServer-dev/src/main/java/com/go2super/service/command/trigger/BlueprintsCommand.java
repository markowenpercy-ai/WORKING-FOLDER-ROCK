package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.ShipPartData;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.*;

public class BlueprintsCommand extends Command {

    public BlueprintsCommand() {

        super("blueprints", "permission.blueprints", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'blueprints' has invalid arguments! (blueprints <guid> [complete])", user);
            return;

        }

        int guid = Integer.parseInt(parts[1]);
        boolean complete = parts.length >= 3 && parts[2].equals("complete");

        User receiver = UserService.getInstance().getUserCache().findByGuid(guid);

        if (receiver == null) {

            sendMessage("Receiver with id " + guid + " does not exists!", user);
            return;

        }

        receiver.getShipUpgrades().setCurrentBodies(new ArrayList<>());
        receiver.getShipUpgrades().setCurrentParts(new ArrayList<>());

        for (ShipBodyData shipBodyData : ResourceManager.getShipBodies().getShipBody()) {
            if (shipBodyData.getLevels() == null || shipBodyData.getLevels().isEmpty()) {
                continue;
            }
            if (complete) {
                int maxLevel = shipBodyData.getLevels().stream()
                    .mapToInt(lv -> lv.getLv())
                    .max()
                    .orElse(0);
                int id = shipBodyData.getLevels().stream()
                    .filter(lv -> lv.getLv() == maxLevel)
                    .findFirst()
                    .map(lv -> lv.getId())
                    .orElse(shipBodyData.getLevels().get(0).getId());
                receiver.getShipUpgrades().getCurrentBodies().add(id);
            } else {
                receiver.getShipUpgrades().getCurrentBodies().add(shipBodyData.getLevels().get(0).getId());
            }
        }

        for (ShipPartData shipPartData : ResourceManager.getShipParts().getShipPart()) {
            if (shipPartData.getLevels() == null || shipPartData.getLevels().isEmpty()) {
                continue;
            }
            if (complete) {
                int maxLevel = shipPartData.getLevels().stream()
                    .mapToInt(lv -> lv.getLv())
                    .max()
                    .orElse(0);
                int id = shipPartData.getLevels().stream()
                    .filter(lv -> lv.getLv() == maxLevel)
                    .findFirst()
                    .map(lv -> lv.getId())
                    .orElse(shipPartData.getLevels().get(0).getId());
                receiver.getShipUpgrades().getCurrentParts().add(id);
            } else {
                receiver.getShipUpgrades().getCurrentParts().add(shipPartData.getLevels().get(0).getId());
            }
        }

        receiver.save();
        sendMessage("All blueprints sent to " + receiver.getUsername() + "!", user);
        sendMessage("You unlock blueprints, please restart client!", receiver);

    }

}

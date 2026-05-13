package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.packet.ship.ResponseDeleteShipTeamBroadcastPacket;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;

import java.util.List;

// didnt test this crap, 90% sure it will let you dismiss fleets if you are in battle



public class DismissAllFleetsCommand extends FleetCommand {

    public DismissAllFleetsCommand() {
        super("fleets_dismissall", "permission.fleetpreset");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (!validateBaseFleetConditions(user, smartServer)) {
            return;
        }

        int galaxyId = user.getPlanet().getPosition().galaxyId();
        List<Fleet> userFleets = getUserBaseFleets(user.getGuid(), galaxyId);
        if (userFleets.isEmpty()) {
            sendMessage("No fleets on base to dismiss", user);
            return;
        }

        UserShips ships = user.getShips();
        PacketService packetService = PacketService.getInstance();

        for (Fleet fleet : userFleets) {
            for (ShipTeamNum cell : fleet.getFleetBody().getCells()) {
                if (cell.getShipModelId() > 0 && cell.getNum() > 0) {
                    ships.addShip(cell.getShipModelId(), cell.getNum());
                }
            }

            ResponseDeleteShipTeamBroadcastPacket response = new ResponseDeleteShipTeamBroadcastPacket();
            response.setGalaxyMapId(0);
            response.setGalaxyId(galaxyId);
            response.setShipTeamId(fleet.getShipTeamId());

            for (var viewer : LoginService.getInstance().getPlanetViewers(galaxyId)) {
                viewer.getSmartServer().send(response);
            }

            packetService.getFleetCache().delete(fleet);
        }

        user.save();
        sendMessage("Dismissed " + userFleets.size() + " fleets, ships returned to bay", user);
    }
}

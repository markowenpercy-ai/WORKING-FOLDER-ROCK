package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserShips;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.packet.ship.ResponseCreateShipTeamPacket;
import com.go2super.service.AutoIncrementService;
import com.go2super.service.BattleService;
import com.go2super.service.CommanderService;
import com.go2super.service.FleetPresetService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;

import java.util.ArrayList;
import java.util.List;

// TODO - check if the cunts dead or wounded before you let them fleet

public class LoadFleetPresetCommand extends FleetCommand {

    public LoadFleetPresetCommand() {
        super("fleets_loadpreset", "permission.fleetpreset");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (parts.length < 2) {
            sendMessage("Usage: /fleets_loadpreset <name>", user);
            return;
        }

        if (!validateBaseFleetConditions(user, smartServer)) {
            return;
        }

        int galaxyId = user.getPlanet().getPosition().galaxyId();
        List<Fleet> existingFleets = getUserBaseFleets(user.getGuid(), galaxyId);
        if (!existingFleets.isEmpty()) {
            sendMessage("Please use /fleets_dismissall first", user);
            return;
        }

        String presetName = parts[1];
        var preset = FleetPresetService.getInstance().getPreset(user.getGuid(), presetName);
        if (preset.isEmpty()) {
            sendMessage("Preset '" + presetName + "' not found", user);
            return;
        }

        var presetEntries = preset.get().getEntries();
        if (presetEntries.isEmpty()) {
            sendMessage("Preset is empty", user);
            return;
        }

        UserShips ships = user.getShips();
        PacketService packetService = PacketService.getInstance();
        CommanderService commanderService = CommanderService.getInstance();

        for (var entry : presetEntries) {
            if (entry.getCommanderId() > 0) {
                Commander commander = commanderService.getCommanderCache().findByCommanderId(entry.getCommanderId());
                if (commander == null) {
                    sendMessage("Error: Commander " + entry.getCommanderId() + " not found", user);
                    return;
                }
                if (packetService.getFleetCache().findByCommanderId(entry.getCommanderId()) != null) {
                    sendMessage("Error: Commander " + entry.getCommanderId() + " is on another fleet", user);
                    return;
                }
            }

            for (ShipTeamNum cell : entry.getCells()) {
                if (cell.getShipModelId() > 0 && cell.getNum() > 0) {
                    ShipTeamNum storedShip = ships.getShipTeamNum(cell.getShipModelId());
                    if (storedShip == null || storedShip.getNum() < cell.getNum()) {
                        sendMessage("Error: Not enough ships (need " + cell.getNum() + " of model " + cell.getShipModelId() + ")", user);
                        return;
                    }
                }
            }
        }

        int created = 0;
        for (var entry : presetEntries) {
            for (ShipTeamNum cell : entry.getCells()) {
                if (cell.getShipModelId() > 0 && cell.getNum() > 0) {
                    ships.removeShip(cell.getShipModelId(), cell.getNum());
                }
            }

            ShipTeamBody teamBody = ShipTeamBody.builder()
                .cells(new ArrayList<>(entry.getCells()))
                .build();

            String fleetName = entry.getName();
            if (fleetName == null || fleetName.isBlank()) {
                fleetName = "Fleet";
            }

            Fleet fleet = Fleet.builder()
                .shipTeamId(AutoIncrementService.getInstance().getNextFleetId())
                .galaxyId(galaxyId)
                .guid(user.getGuid())
                .name(fleetName)
                .he3(entry.getHe3())
                .commanderId(entry.getCommanderId())
                .bodyId(entry.getBodyId())
                .rangeType(entry.getRangeType())
                .preferenceType(entry.getPreferenceType())
                .posX(entry.getPosX())
                .posY(entry.getPosY())
                .direction(entry.getDirection())
                .match(false)
                .fleetBody(teamBody)
                .build();

            packetService.getFleetCache().save(fleet);

            ResponseCreateShipTeamPacket response = new ResponseCreateShipTeamPacket();
            response.setGalaxyMapId(0);
            response.setGalaxyId(galaxyId);

            GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();
            fleetInfo.setShipTeamId(fleet.getShipTeamId());
            fleetInfo.setShipNum(fleet.ships());
            fleetInfo.setBodyId((short) fleet.getBodyId());
            fleetInfo.setReserve((short) 0);
            fleetInfo.setDirection((byte) fleet.getDirection());
            fleetInfo.setPosX((byte) fleet.getPosX());
            fleetInfo.setPosY((byte) fleet.getPosY());
            fleetInfo.setOwner((byte) 0);
            response.setGalaxyFleetInfo(fleetInfo);

            for (var viewer : LoginService.getInstance().getPlanetViewers(galaxyId)) {
                fleetInfo.setOwner((byte) BattleService.getInstance().getFleetColor(viewer, fleet));
                viewer.getSmartServer().send(response);
            }

            created++;
        }

        user.save();
        sendMessage("Loaded preset '" + presetName + "' with " + created + " fleets", user);
    }
}

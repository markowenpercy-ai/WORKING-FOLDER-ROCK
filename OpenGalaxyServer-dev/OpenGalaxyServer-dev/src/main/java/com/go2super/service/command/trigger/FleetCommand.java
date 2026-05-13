package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.PacketService;
import com.go2super.service.command.Command;

import java.util.List;

public abstract class FleetCommand extends Command {

    public FleetCommand(String name, String permission) {
        super(name, permission);
    }

    protected boolean isUserOnline(User user) {
        return user.isOnline();
    }

    protected boolean isUserInWar(User user) {
        UserPlanet planet = user.getPlanet();
        return planet != null && planet.isInWar();
    }

    protected boolean hasFleetsInTransmission(int guid) {
        return !PacketService.getInstance().getFleetCache().getInTransmissionFleets(guid).isEmpty();
    }

    protected List<Fleet> getUserBaseFleets(int guid, int galaxyId) {
        return PacketService.getInstance().getFleetCache().findAllByGalaxyId(galaxyId).stream()
            .filter(f -> f.getGuid() == guid && !f.isMatch() && f.getFleetTransmission() == null)
            .toList();
    }

    protected boolean validateBaseFleetConditions(User user, SmartServer smartServer) {
        if (!isUserOnline(user)) {
            sendMessage("You must be online to use this command", user);
            return false;
        }

        UserPlanet planet = user.getPlanet();
        if (planet == null) {
            sendMessage("Planet not found", user);
            return false;
        }

        if (isUserInWar(user)) {
            sendMessage("Cannot use this command while in battle", user);
            return false;
        }

        if (hasFleetsInTransmission(user.getGuid())) {
            sendMessage("Cannot use this command while fleets are in transmission", user);
            return false;
        }

        return true;
    }
}

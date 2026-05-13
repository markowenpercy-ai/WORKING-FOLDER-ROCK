package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.FleetPresetService;

import java.util.List;

public class SaveFleetPresetCommand extends FleetCommand {

    public SaveFleetPresetCommand() {
        super("fleets_savepreset", "permission.fleetpreset");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (parts.length < 2) {
            sendMessage("Usage: /fleets_savepreset <name>", user);
            return;
        }

        if (!validateBaseFleetConditions(user, smartServer)) {
            return;
        }

        String presetName = parts[1];

        List<Fleet> userFleets = getUserBaseFleets(user.getGuid(), user.getPlanet().getPosition().galaxyId());
        if (userFleets.isEmpty()) {
            sendMessage("No fleets on base to save", user);
            return;
        }

        try {
            FleetPresetService.getInstance().savePreset(user.getGuid(), presetName, userFleets);
            sendMessage("Preset '" + presetName + "' saved with " + userFleets.size() + " fleets", user);
        } catch (IllegalStateException e) {
            sendMessage("Error: " + e.getMessage(), user);
        }
    }
}

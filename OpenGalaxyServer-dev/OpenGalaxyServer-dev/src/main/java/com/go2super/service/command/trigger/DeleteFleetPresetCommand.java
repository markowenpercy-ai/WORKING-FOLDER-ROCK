package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.FleetPresetService;

public class DeleteFleetPresetCommand extends FleetCommand {

    public DeleteFleetPresetCommand() {
        super("fleets_deletepreset", "permission.fleetpreset");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (parts.length < 2) {
            sendMessage("Usage: /fleets_deletepreset <name>", user);
            return;
        }

        String presetName = parts[1];

        if (!FleetPresetService.getInstance().presetExists(user.getGuid(), presetName)) {
            sendMessage("Preset '" + presetName + "' not found", user);
            return;
        }

        FleetPresetService.getInstance().deletePreset(user.getGuid(), presetName);
        sendMessage("Preset '" + presetName + "' deleted", user);
    }
}

package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.FleetPresetService;

public class ListFleetPresetsCommand extends FleetCommand {

    public ListFleetPresetsCommand() {
        super("fleets_listpresets", "permission.fleetpreset");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        var presets = FleetPresetService.getInstance().listPresets(user.getGuid());
        if (presets.isEmpty()) {
            sendMessage("No fleet presets saved", user);
            return;
        }

        sendMessage("Fleet presets:", user);
        for (var preset : presets) {
            sendMessage("  - " + preset.getName() + " (" + preset.getEntries().size() + " fleets)", user);
        }
    }
}

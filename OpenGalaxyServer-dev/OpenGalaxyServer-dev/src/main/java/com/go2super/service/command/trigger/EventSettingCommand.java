package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.CLIEventService;
import com.go2super.service.command.Command;

// this shits pissing me off i want to be able to change teh fucking event amt

public class EventSettingCommand extends Command {

    public EventSettingCommand() {
        super("eventsettings", "permission.eventsettings");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (parts.length == 2) {
            if (parts[1].equalsIgnoreCase("enable")) {
                CLIEventService.getInstance().setEventEnabled(true);
                sendMessage("Event enabled!", user);
            } else if (parts[1].equalsIgnoreCase("disable")) {
                CLIEventService.getInstance().setEventEnabled(false);
                sendMessage("Event disabled!", user);
            } else if (parts[1].equalsIgnoreCase("refresh")) {
                CLIEventService.getInstance().setEventEnabled(false);
                sendMessage("Event disabled!", user);
                CLIEventService.getInstance().resetStoreEventForAll();
                sendMessage("Event refreshed!", user);
                CLIEventService.getInstance().setEventEnabled(true);
                sendMessage("Event enabled!", user);
            }
        }
    }
}

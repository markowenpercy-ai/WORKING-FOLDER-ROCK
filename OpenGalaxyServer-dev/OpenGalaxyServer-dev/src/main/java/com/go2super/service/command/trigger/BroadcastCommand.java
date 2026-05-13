package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.ChatService;
import com.go2super.service.command.Command;

public class BroadcastCommand extends Command {

    public BroadcastCommand() {

        super("broadcast", "permission.broadcast", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {

            sendMessage("Command 'broadcast' need more arguments!", user);
            return;

        }

        String message = "";
        for (int i = 1; i < parts.length; i++) {
            message += parts[i] + " ";
        }

        message = message.trim();
        ChatService.getInstance().broadcastMessage(message);

    }

}

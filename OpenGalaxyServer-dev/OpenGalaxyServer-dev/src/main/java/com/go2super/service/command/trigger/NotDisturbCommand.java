package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.command.Command;

public class NotDisturbCommand extends Command {

    public NotDisturbCommand() {

        super("notdisturb", "permission.notdisturb");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (user.isNotDisturb()) {
            sendMessage("Do not disturb mode disabled!", user);
            user.setNotDisturb(false);
            user.save();
            return;
        }

        sendMessage("Do not disturb mode activated!", user);
        user.setNotDisturb(true);
        user.save();

    }
}

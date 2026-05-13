package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.command.Command;



public class HasBlockCommand extends Command {

    public HasBlockCommand() {

        super("hasblock", "permission.hasblock", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {
            sendMessage("Command 'hasblock' has invalid arguments! (hasblock <guid>)", user);
            return;
        }

        if (user.getBlockUsers() == null || user.getBlockUsers().size() == 0) {
            sendMessage("The list is empty!", user);
            return;
        }

        int guid = Integer.parseInt(parts[1]);
        for (int i = 0; i < user.getBlockUsers().size(); i++) {
            if (user.getBlockUsers().get(i).intValue() == guid) {
                sendMessage("This user is blocked!", user);
                return;
            }
            sendMessage("This user is not blocked!", user);
            return;
        }
    }
}


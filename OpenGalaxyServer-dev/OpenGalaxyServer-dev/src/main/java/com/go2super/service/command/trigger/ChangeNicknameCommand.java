package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

public class ChangeNicknameCommand extends Command {

    public ChangeNicknameCommand() {

        super("changenickname", "permission.changenickname");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 3) {

            sendMessage("Command 'changenickname' has invalid arguments! (changenickname <guid> <name>)", user);
            return;

        }

        int guid = Integer.parseInt(parts[1]);
        String name = parts[2];

        if (name.length() > 32) {
            sendMessage("Nickname is too long!", user);
            return;
        }

        User receiver = UserService.getInstance().getUserCache().findByGuid(guid);

        if (receiver == null) {

            sendMessage("Receiver with id " + guid + " does not exists!", user);
            return;

        }

        String current = receiver.getUsername();
        receiver.setUsername(name);
        receiver.save();

        sendMessage("Username changed from " + current + " to " + name + "!", user);

    }

}

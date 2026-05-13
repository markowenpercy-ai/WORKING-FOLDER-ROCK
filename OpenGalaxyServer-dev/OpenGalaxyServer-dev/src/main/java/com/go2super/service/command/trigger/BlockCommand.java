package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.*;

public class BlockCommand extends Command {

    public BlockCommand() {

        super("block", "permission.block", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (parts.length < 2) {
            sendMessage("Command 'block' has invalid arguments! (block <guid>)", user);
            return;
        }

        if (user.getBlockUsers() == null) {
            user.setBlockUsers(new ArrayList<>());
        }

        int guid = Integer.parseInt(parts[1]);
        List<Integer> blockUsers = user.getBlockUsers();
        User blockedUser = UserService.getInstance().getUserCache().findByGuid(guid);

        if (user.getBlockUsers().size() == 30) {
            sendMessage("You have reached the limit of blocked users!", user);
            return;
        }

        if (user.getGuid() == guid) {
            sendMessage("Why are you going to block yourself? you have no reason to do so!", user);
            return;
        }

        for (int i = 0; i < blockUsers.size(); i++) {
            if (blockUsers.get(i).intValue() == guid) {
                blockUsers.remove(Integer.valueOf(guid));
                user.save();
                sendMessage("The user has been unlocked! [" + blockedUser.getUsername() + "]", user);
                return;
            }
        }

        blockUsers.add(guid);
        user.save();
        sendMessage("The user has been blocked! [" + blockedUser.getUsername() + "]", user);

    }

}

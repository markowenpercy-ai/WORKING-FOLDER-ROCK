package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.*;

public class BlackListCommand extends Command {

    public BlackListCommand() {

        super("blacklist", "permission.blackList", "permission.qa");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {

        if (user.getBlockUsers() == null || user.getBlockUsers().size() == 0) {
            sendMessage("The list is empty!", user);
            return;
        }

        List<Integer> blockUsers = user.getBlockUsers();
        List<String> names = new ArrayList<>();

        for (int i = 0; i < blockUsers.size(); i++) {
            User blockedUser = UserService.getInstance().getUserCache().findByGuid(blockUsers.get(i).intValue());
            if (blockedUser == null) {
                continue;
            }
            names.add(blockedUser.getUsername());
            if (i == 4) {
                break;
            }
        }

        if (blockUsers.size() > 5) {
            sendMessage(names + " and " + (blockUsers.size() - names.size()) + " more...", user);
            return;
        }

        sendMessage(names + "", user);

    }
}

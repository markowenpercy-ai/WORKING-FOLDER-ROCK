package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.CommanderService;
import com.go2super.service.UserService;
import com.go2super.service.command.Command;

import java.util.List;

public class MaxCommandersCommand extends Command {

    public MaxCommandersCommand() {
        super("maxcommanders", "permission.giveuser");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (account.getUserRank() != UserRank.ADMIN) {
            sendMessage("This command is only available to ADMIN users!", user);
            return;
        }

        if (parts.length < 2) {
            sendMessage("Usage: /maxcommanders <userId>", user);
            return;
        }

        int userId = Integer.parseInt(parts[1]);

        User targetUser = UserService.getInstance().getUserCache().findByGuid(userId);
        if (targetUser == null) {
            sendMessage("User not found: " + userId, user);
            return;
        }

        List<Commander> commanders = CommanderService.getInstance().getCommanderCache().findByUserId(targetUser.getUserId());
        
        if (commanders.isEmpty()) {
            sendMessage("No commanders found for user: " + userId, user);
            return;
        }

        int count = 0;
        for (Commander commander : commanders) {
            commander.setExperience(CommanderService.MAXIMUM_EXPERIENCE);
            commander.setLevel(49);
            commander.save();
            count++;
        }

        sendMessage("Maxed " + count + " commanders for user " + userId + " (Level 50, EXP " + CommanderService.MAXIMUM_EXPERIENCE + ")", user);
    }
}

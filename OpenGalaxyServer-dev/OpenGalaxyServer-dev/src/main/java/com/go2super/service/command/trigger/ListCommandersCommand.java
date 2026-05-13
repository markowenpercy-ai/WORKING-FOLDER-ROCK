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

public class ListCommandersCommand extends Command {

    public ListCommandersCommand() {
        super("listcommanders", "permission.listcommanders");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        User targetUser = user;
        boolean isViewingSelf = true;

        if (account.getUserRank() == UserRank.ADMIN && parts.length >= 2) {
            int userId = Integer.parseInt(parts[1]);
            targetUser = UserService.getInstance().getUserCache().findByGuid(userId);
            if (targetUser == null) {
                sendMessage("User not found: " + userId, user);
                return;
            }
            isViewingSelf = (userId == user.getGuid());
        } else if (account.getUserRank() != UserRank.ADMIN && parts.length >= 2) {
            int userId = Integer.parseInt(parts[1]);
            if (userId != user.getGuid()) {
                sendMessage("You can only view your own commanders!", user);
                return;
            }
        }

        List<Commander> commanders = CommanderService.getInstance().getCommanderCache().findByUserId(targetUser.getUserId());
        
        if (commanders.isEmpty()) {
            sendMessage("No commanders found", user);
            return;
        }

        String header = isViewingSelf ? "Your commanders:" : "User " + targetUser.getUserId() + "'s commanders:";
        sendMessage(header, user);
        for (Commander commander : commanders) {
            sendMessage(commander.getCommanderId() + ": " + commander.getName() + " (Variance: " + commander.getVariance() + ")", user);
        }
    }
}

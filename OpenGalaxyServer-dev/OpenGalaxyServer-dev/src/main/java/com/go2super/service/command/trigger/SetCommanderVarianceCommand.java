package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.CommanderService;
import com.go2super.service.command.Command;


public class SetCommanderVarianceCommand extends Command {

    public SetCommanderVarianceCommand() {
        super("setcommandervariance", "permission.giveuser");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (account.getUserRank() != UserRank.ADMIN) {
            sendMessage("This command is only available to ADMIN users!", user);
            return;
        }

        if (parts.length < 3) {
            sendMessage("Usage: /setcommandervariance <commanderId> <variance>", user);
            return;
        }

        int commanderId = Integer.parseInt(parts[1]);
        int variance = Integer.parseInt(parts[2]);

        Commander commander = CommanderService.getInstance().getCommanderCache().findByCommanderId(commanderId);
        if (commander == null) {
            sendMessage("Commander not found: " + commanderId, user);
            return;
        }

        variance = Math.min(Math.max(variance, -CommanderService.MAX_VARIANCE), CommanderService.MAX_VARIANCE);

        commander.setVariance(variance);
        commander.save();

        sendMessage("Updated commander " + commanderId + " variance to " + variance, user);
    }
}

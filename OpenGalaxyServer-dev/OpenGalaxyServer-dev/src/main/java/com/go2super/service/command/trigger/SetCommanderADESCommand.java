package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.Commander;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.UserRank;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.CommanderService;
import com.go2super.service.command.Command;

// TODO - stop letting this set all stats to 5 lol

public class SetCommanderADESCommand extends Command {

    public SetCommanderADESCommand() {
        super("setcommanderstats", "permission.giveuser");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) {
        if (account.getUserRank() != UserRank.ADMIN) {
            sendMessage("This command is only available to ADMIN users!", user);
            return;
        }

        if (parts.length < 6) {
            sendMessage("Usage: /setcommanderstats <commanderId> <aim> <dodge> <speed> <electron>", user);
            return;
        }

        int commanderId = Integer.parseInt(parts[1]);
        int aim = Integer.parseInt(parts[2]);
        int dodge = Integer.parseInt(parts[3]);
        int speed = Integer.parseInt(parts[4]);
        int electron = Integer.parseInt(parts[5]);

        Commander commander = CommanderService.getInstance().getCommanderCache().findByCommanderId(commanderId);
        if (commander == null) {
            sendMessage("Commander not found: " + commanderId, user);
            return;
        }

        aim = Math.min(Math.max(aim, 0), CommanderService.MAX_GROWTH);
        dodge = Math.min(Math.max(dodge, 0), CommanderService.MAX_GROWTH);
        speed = Math.min(Math.max(speed, 0), CommanderService.MAX_GROWTH);
        electron = Math.min(Math.max(electron, 0), CommanderService.MAX_GROWTH);

        commander.setGrowthAim(aim);
        commander.setGrowthDodge(dodge);
        commander.setGrowthSpeed(speed);
        commander.setGrowthElectron(electron);
        commander.save();

        sendMessage("Updated commander " + commanderId + " ADES: Aim=" + aim + ", Dodge=" + dodge + ", Speed=" + speed + ", Electron=" + electron, user);
    }
}

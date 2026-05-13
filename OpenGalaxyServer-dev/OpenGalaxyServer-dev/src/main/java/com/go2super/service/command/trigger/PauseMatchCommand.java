package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.obj.entry.SmartServer;
import com.go2super.service.BattleService;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.command.Command;

import java.io.IOException;

public class PauseMatchCommand extends Command {
    public PauseMatchCommand() {
        super("pausematch", "permission.match");
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) throws IOException, InterruptedException {
        if (parts.length < 2) {
            sendMessage("Please specify a match id ", user);
            return;
        }
        String matchId = parts[1];
        GameBattle battle = BattleService.getInstance().findById(matchId);
        if (battle == null) {
            sendMessage("Match not found", user);
            return;
        }

        Match match = battle.getMatch();
        if (match.getPause().get()) {
            sendMessage("Match is already paused", user);
            return;
        }

        match.getPause().set(true);
        sendMessage("Match paused", user);
    }
}

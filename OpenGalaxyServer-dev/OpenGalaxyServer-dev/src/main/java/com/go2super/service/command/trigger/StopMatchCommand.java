package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.MatchType;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.type.AuditType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.BattleService;
import com.go2super.service.DiscordService;
import com.go2super.service.command.Command;

import java.awt.*;
import java.io.IOException;
import java.util.Date;

public class StopMatchCommand extends Command {

    public StopMatchCommand() {

        super("stopmatch", "permission.match");
    }


    private enum StopMatchType {
        ALL("all", null),
        LEAGUE("league", MatchType.LEAGUE_MATCH),
        ARENA("arena", MatchType.LEAGUE_MATCH),
        CHAMPS("champs", MatchType.CHAMPION_MATCH),
        INSTANCE("instance", MatchType.INSTANCE_MATCH),
        WAR("war", MatchType.PVP_MATCH),
        RBP_MATCH("rbp", MatchType.RBP_MATCH),
        HUMAROID("humaroid", MatchType.HUMAROID_MATCH),

        PIRATE("pirates", MatchType.PIRATES_MATCH),

        ID("id", null),

        COORDINATE("coordinate", null),

        ;

        private final String name;
        private final MatchType matchType;

        StopMatchType(String name, MatchType matchType) {
            this.name = name;
            this.matchType = matchType;
        }
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) throws IOException, InterruptedException {
        // command example: /stopmatch all/league/arena/champs/instance/user/war
        // command 2 example /stopmatch id <matchId>

        if (parts.length < 2) {
            sendMessage("Please specify a match type to stop.", user);
            return;
        }

        StopMatchType stopMatchType = null;
        for (StopMatchType type : StopMatchType.values()) {
            if (type.name.equalsIgnoreCase(parts[1])) {
                stopMatchType = type;
                break;
            }
        }
        BattleService battleService = BattleService.getInstance();
        if (stopMatchType == null) {
            sendMessage("Invalid match type.", user);
            return;
        }

        if (stopMatchType == StopMatchType.ID) {
            if (parts.length < 3) {
                sendMessage("Please specify a match id to stop.", user);
                return;
            }
            String matchId = parts[2];
            boolean b = battleService.stopMatch(matchId);
            if (!b) {
                sendMessage("Match not stopped.", user);
                return;
            }
            sendMessage("Stopped match " + matchId, user);
            DiscordService.getInstance().getRayoBot().sendAudit("System",
                    "User " + user.getUsername() + " stopped match " + matchId + " on " + new Date(), Color.MAGENTA, AuditType.CHAT);

            return;
        }

        if (stopMatchType == StopMatchType.COORDINATE) {
            if (parts.length < 4) {
                sendMessage("Please specify a coordinate to stop.", user);
                return;
            }
            GalaxyTile coordinate = new GalaxyTile(Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
            sendMessage("Stopped coordinate " + coordinate, user);
            battleService.stopCoordinate(coordinate);
            DiscordService.getInstance().getRayoBot().sendAudit("System",
                    "User " + user.getUsername() + " stopped coordinate " + coordinate + " on " + new Date(), Color.MAGENTA, AuditType.CHAT);
            return;
        }

        if (stopMatchType == StopMatchType.ALL) {
            battleService.stopAllMatches();
            sendMessage("Stopped all matches.", user);
            DiscordService.getInstance().getRayoBot().sendAudit("System",
                    "User " + user.getUsername() + " stopped all matches on " + new Date(), Color.MAGENTA, AuditType.CHAT);
            return;
        }

        battleService.stopMatches(stopMatchType.matchType);
        sendMessage("Stopped all " + stopMatchType.name + " matches.", user);
        DiscordService.getInstance().getRayoBot().sendAudit("System",
                "User " + user.getUsername() + " stopped all " + stopMatchType.name + " matches on " + new Date(), Color.MAGENTA, AuditType.CHAT);
    }
}

package com.go2super.service.command.trigger;

import com.go2super.database.entity.Account;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.MatchType;
import com.go2super.obj.entry.SmartServer;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.BattleService;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.command.Command;

import java.io.IOException;
import java.util.List;

public class FindMatchCommand extends Command {

    public FindMatchCommand() {

        super("findmatch", "permission.match");
    }


    private enum FindMatchType {
        LEAGUE("league", MatchType.LEAGUE_MATCH),
        ARENA("arena", MatchType.LEAGUE_MATCH),
        CHAMPS("champs", MatchType.CHAMPION_MATCH),
        INSTANCE("instance", MatchType.INSTANCE_MATCH),
        WAR("war", MatchType.PVP_MATCH),
        RBP_MATCH("rbp", MatchType.RBP_MATCH),
        HUMAROID_MATCH("humaroid", MatchType.HUMAROID_MATCH),
        PIRATES_MATCH("pirates", MatchType.PIRATES_MATCH),
        IGL_MATCH("igl", MatchType.IGL_MATCH),

        COORDINATE("coordinate", null),

        USER("guid", null),
        ;

        private final String name;
        private final MatchType matchType;

        FindMatchType(String name, MatchType matchType) {
            this.name = name;
            this.matchType = matchType;
        }
    }

    @Override
    public void execute(User user, Account account, SmartServer smartServer, String label, String[] parts) throws IOException, InterruptedException {
        // command example: /findmatch league/arena/champs/instance/user/war

        if (parts.length < 2) {
            sendMessage("Please specify a match type to find.", user);
            return;
        }

        FindMatchType findMatchType = null;
        for (FindMatchType type : FindMatchType.values()) {
            if (type.name.equalsIgnoreCase(parts[1])) {
                findMatchType = type;
                break;
            }
        }

        BattleService battleService = BattleService.getInstance();
        if (findMatchType == null) {
            sendMessage("Invalid match type.", user);
            return;
        }

        if (findMatchType == FindMatchType.COORDINATE) {
            if (parts.length < 4) {
                sendMessage("Please specify a coordinate to find.", user);
                return;
            }
            int posX = Integer.parseInt(parts[2]);
            int posY = Integer.parseInt(parts[3]);
            GalaxyTile galaxyTile = new GalaxyTile(posX, posY);
            Match match = battleService.findBy(galaxyTile);
            if (match == null) {
                sendMessage("No match found.", user);
                return;
            }
            String msg = "Match Id: %s Match Type:%s Ended:%s Paused:%s Can Continue:%s";
            sendMessage(String.format(msg, match.getId(), match.getMatchType(), match.isEnded(), match.getPause().get(), match.canContinue()), user);
            return;
        }

        if (findMatchType == FindMatchType.USER) {
            if (parts.length < 3) {
                sendMessage("Please specify a guid to find.", user);
                return;
            }
            int guid = Integer.parseInt(parts[2]);
            List<GameBattle> battles = battleService.findByGuid(guid);
            if (battles.isEmpty()) {
                sendMessage("No match found.", user);
                return;
            }
            for (GameBattle battle : battles) {
                Match match = battle.getMatch();
                String msg = "Match Id: %s Match Type:%s Ended:%s Paused:%s Can Continue:%s";
                sendMessage(String.format(msg, match.getId(), match.getMatchType(), match.isEnded(), match.getPause().get(), match.canContinue()), user);
            }
            return;
        }

        List<GameBattle> battlesMatchType = battleService.findBattlesMatchType(findMatchType.matchType);
        if (battlesMatchType.isEmpty()) {
            sendMessage("No match found.", user);
            return;
        }

        for (GameBattle battle : battlesMatchType) {
            Match match = battle.getMatch();
            String msg = "Match Id: %s Match Type:%s Ended:%s Paused:%s";
            sendMessage(String.format(msg, match.getId(), match.getMatchType(), match.isEnded(), match.getPause().get()), user);
        }


    }
}

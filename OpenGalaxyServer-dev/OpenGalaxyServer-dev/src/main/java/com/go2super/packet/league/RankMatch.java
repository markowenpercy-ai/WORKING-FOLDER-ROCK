package com.go2super.packet.league;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.User;
import com.go2super.obj.BufferObject;
import com.go2super.obj.game.UserLeagueLeaderboard;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RankMatch extends BufferObject {
    private String name;
    private int guid;
    private int matchWeekTop;
    private byte matchLevel;
    private byte matchWin;
    private byte matchLost;
    private byte matchDogfall;

    @Override
    public void write(Go2Buffer go2buffer) {
        go2buffer.addString(SmartString.of(name, VariableType.MAX_NAME));
        go2buffer.addInt(guid);
        go2buffer.addInt(matchWeekTop);
        go2buffer.addChar(matchLevel);
        go2buffer.addChar(matchWin);
        go2buffer.addChar(matchLost);
        go2buffer.addChar(matchDogfall);
    }

    public static RankMatch from(User user, UserLeagueLeaderboard rank) {
        var m = new RankMatch();
        m.setName(user.getUsername());
        m.setGuid(user.getGuid());
        m.setMatchWeekTop(rank.getRank());
        m.setMatchLevel((byte) rank.getLeague());
        m.setMatchWin((byte) rank.getWins());
        m.setMatchLost((byte) rank.getLosses());
        m.setMatchDogfall((byte) rank.getDraws());
        return m;
    }

    @Override
    public RankMatch trash() {
        return new RankMatch("", -1, 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0);
    }
}

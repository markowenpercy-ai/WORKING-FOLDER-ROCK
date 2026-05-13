package com.go2super.obj.game;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserLeagueLeaderboard {
    private int guid;
    private int wins;
    private int losses;
    private int draws;
    private int league;
    private int rank;

    public int getPoints() {
        return wins * 3 + draws + losses;
    }
}

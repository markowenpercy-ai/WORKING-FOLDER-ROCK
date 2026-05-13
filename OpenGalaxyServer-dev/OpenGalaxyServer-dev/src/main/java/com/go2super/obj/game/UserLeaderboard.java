package com.go2super.obj.game;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UserLeaderboard {

    private int guid;

    private long attackPower;
    private long shootdowns;
    private long league;

}

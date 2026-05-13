package com.go2super.database.entity.sub;

import lombok.Data;

@Data
public class BattleExpCache {

    private int guid;
    private int commanderId;

    private int exp;
    private int headId;
    private int levelId;

}

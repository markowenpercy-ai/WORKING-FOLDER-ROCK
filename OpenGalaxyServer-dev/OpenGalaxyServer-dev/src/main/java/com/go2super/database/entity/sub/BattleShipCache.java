package com.go2super.database.entity.sub;

import lombok.Data;

@Data
public class BattleShipCache {

    private int guid;
    private int shipModelId;

    private double shootdowns;
    private double highestAttack;

}

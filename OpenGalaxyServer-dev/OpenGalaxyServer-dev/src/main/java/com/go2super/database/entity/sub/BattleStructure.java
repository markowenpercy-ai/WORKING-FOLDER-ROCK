package com.go2super.database.entity.sub;

import lombok.Data;

@Data
public class BattleStructure {

    private int buildingId;
    private int level;

    private int health;
    private int maxHealth;

    private boolean destroyed = false;

}

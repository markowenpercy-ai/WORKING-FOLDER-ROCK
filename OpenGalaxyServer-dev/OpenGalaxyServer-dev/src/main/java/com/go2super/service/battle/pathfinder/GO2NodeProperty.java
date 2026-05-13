package com.go2super.service.battle.pathfinder;

import lombok.Data;

@Data
public class GO2NodeProperty {
    private int minRange;
    private int maxRange;
    private int idealRange;
    private int movement;
    private GO2Node target;
}

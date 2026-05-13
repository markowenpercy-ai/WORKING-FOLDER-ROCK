package com.go2super.service.battle.comparator;

import com.go2super.database.entity.sub.BattleElement;
import com.go2super.database.entity.sub.BattleFleet;
import lombok.Getter;

import java.util.*;

@Getter
public abstract class IntervalComparator implements Comparator<BattleElement> {

    private final BattleFleet attacker;

    public IntervalComparator(BattleFleet attacker) {

        this.attacker = attacker;
    }

}

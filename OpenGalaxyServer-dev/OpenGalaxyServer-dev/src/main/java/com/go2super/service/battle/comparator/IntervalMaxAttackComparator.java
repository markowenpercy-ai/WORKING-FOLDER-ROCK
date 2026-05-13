package com.go2super.service.battle.comparator;

import com.go2super.database.entity.sub.BattleElement;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.type.BattleElementType;

public class IntervalMaxAttackComparator extends IntervalComparator {

    public IntervalMaxAttackComparator(BattleFleet attacker) {

        super(attacker);
    }

    @Override
    public int compare(BattleElement element1, BattleElement element2) {

        if (element1.getType() != BattleElementType.FLEET) {
            return 1;
        }
        if (element2.getType() != BattleElementType.FLEET) {
            return -1;
        }

        BattleFleet fleet1 = (BattleFleet) element1;
        BattleFleet fleet2 = (BattleFleet) element2;

        long attackPower1 = fleet1.getRoundAttack();
        long attackPower2 = fleet2.getRoundAttack();

        if (attackPower1 > attackPower2) {
            return -1;
        }
        if (attackPower1 < attackPower2) {
            return 1;
        }

        return fleet1.compareTo(fleet2);

    }

}

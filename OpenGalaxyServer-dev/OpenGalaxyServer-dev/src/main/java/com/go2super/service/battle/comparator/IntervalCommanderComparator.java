package com.go2super.service.battle.comparator;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BattleElement;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.type.BattleElementType;

public class IntervalCommanderComparator extends IntervalComparator {

    public IntervalCommanderComparator(BattleFleet attacker) {

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

        BattleCommander commander1 = fleet1.getBattleCommander();
        BattleCommander commander2 = fleet2.getBattleCommander();

        if (commander1.getStars() > commander2.getStars()) {
            return -1;
        }
        if (commander1.getStars() < commander2.getStars()) {
            return 1;
        }

        if (fleet1.getShipTeamId() < fleet2.getShipTeamId()) {
            return -1;
        }

        if (fleet1.getShipTeamId() > fleet2.getShipTeamId()) {
            return 1;
        }

        return fleet1.compareTo(fleet2);

    }

}

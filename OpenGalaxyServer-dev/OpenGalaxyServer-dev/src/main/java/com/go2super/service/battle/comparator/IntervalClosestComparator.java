package com.go2super.service.battle.comparator;

import com.go2super.database.entity.sub.BattleElement;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.type.BattleElementType;
import com.go2super.service.battle.astar.Node;

public class IntervalClosestComparator extends IntervalComparator {

    public IntervalClosestComparator(BattleFleet attacker) {

        super(attacker);
    }

    @Override
    public int compare(BattleElement element1, BattleElement element2) {

        Node attackerNode = getAttacker().getNode();

        Node fleet1Node = element1.getNode();
        Node fleet2Node = element2.getNode();

        int distance1 = fleet1Node.getHeuristic(attackerNode);
        int distance2 = fleet2Node.getHeuristic(attackerNode);

        if (distance1 > distance2) {
            return 1;
        }
        if (distance1 < distance2) {
            return -1;
        }
        if (element1.getType() != BattleElementType.FLEET || element2.getType() != BattleElementType.FLEET) {
            return 0;
        }

        BattleFleet fleet1 = (BattleFleet) element1;
        BattleFleet fleet2 = (BattleFleet) element2;

        return fleet1.compareTo(fleet2);

    }

}

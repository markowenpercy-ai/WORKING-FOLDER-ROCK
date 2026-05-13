package com.go2super.service.battle.comparator;

import com.go2super.database.entity.sub.BattleElement;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.type.BattleElementType;
import com.go2super.service.battle.astar.Node;

public class IntervalBuildingsComparator extends IntervalComparator {

    public IntervalBuildingsComparator(BattleFleet attacker) {

        super(attacker);
    }

    @Override
    public int compare(BattleElement element1, BattleElement element2) {

        if (element1.getType() == BattleElementType.FORTIFICATION && element2.getType() == BattleElementType.FORTIFICATION) {
            Node attackerNode = getAttacker().getNode();
            Node node1 = element1.getNode();
            Node node2 = element2.getNode();
            int distance1 = node1.getHeuristic(attackerNode);
            int distance2 = node2.getHeuristic(attackerNode);
            if (distance1 > distance2) {
                return 1;
            }
            if (distance1 < distance2) {
                return -1;
            }
            return 0;
        }

        if (element1.getType() == BattleElementType.FORTIFICATION) {
            return -1;
        }
        if (element2.getType() == BattleElementType.FORTIFICATION) {
            return 1;
        }

        return 0;

    }

}

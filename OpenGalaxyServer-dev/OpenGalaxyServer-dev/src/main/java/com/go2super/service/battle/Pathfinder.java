package com.go2super.service.battle;

import com.go2super.service.battle.astar.AStar;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.pathfinder.GO2Node;
import com.go2super.service.battle.pathfinder.GO2Path;
import com.go2super.service.battle.pathfinder.GO2Pathfinder;
import com.go2super.service.battle.type.Target;

import java.util.*;

public class Pathfinder {

    public static GO2Path get2Pathing(BattleCell starter, BattleCell target, int[][] blocks, int minRange, int maxRange, int movement, Target config) {

        GO2Node startNode = new GO2Node(starter.getX(), starter.getY());
        GO2Node targetNode = new GO2Node(target.getX(), target.getY());

        GO2Pathfinder go2Pathfinder = new GO2Pathfinder(startNode, targetNode, blocks, minRange, maxRange, movement, config);
        return go2Pathfinder.findPath();

    }

    public static List<Node> getPathing(BattleCell starter, BattleCell target, int range, int[][] obstacles) {

        Node initialNode = new Node(starter.getX(), starter.getY());
        Node finalNode = new Node(target.getX(), target.getY());

        int rows = 25;
        int cols = 25;

        AStar finder = new AStar(rows, cols, initialNode, finalNode, range);
        finder.setBlocks(obstacles);

        return finder.findPath();

    }

}

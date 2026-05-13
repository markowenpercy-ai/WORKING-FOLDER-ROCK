package com.go2super.service.battle.pathfinder;

import com.go2super.logger.BotLogger;
import com.go2super.service.battle.type.Target;
import org.checkerframework.checker.units.qual.A;

import java.util.*;

public class GO2Pathfinder {

    private final GO2Node start;
    private final GO2Node target;

    private final int[][] blocks;

    private final int minRange;
    private final int maxRange;
    private final int idealRange;

    private final int movement;

    public GO2Pathfinder(GO2Node start, GO2Node target, int[][] blocks, int minRange, int maxRange, int movement, Target config) {

        this.start = start;
        this.target = target;
        this.blocks = blocks;
        this.minRange = minRange;
        this.maxRange = maxRange;
        this.movement = movement;

        this.idealRange = config == Target.MIN_RANGE ? minRange : maxRange;

    }


    public GO2Path findPath() {

        List<GO2Path> paths = findPaths(movement);

        if (paths == null || paths.isEmpty()) {
            return null;
        }
        //logic for best calculated path
        var targetAble = new ArrayList<>(paths.stream().filter(x -> x.getEnd().isIdeal()).toList());
        if(!targetAble.isEmpty()){
            targetAble.sort((a, b) -> {
                if(a.getNodes().size() > b.getNodes().size()){
                    return 1;
                }
                if(a.getNodes().size() < b.getNodes().size()){
                    return -1;
                }
                if (a.turns() > b.turns()) {
                    return 1;
                }
                if (a.turns() < b.turns()) {
                    return -1;
                }
                return 0;
            });

            return targetAble.get(0);
        }
        targetAble = new ArrayList<>(paths.stream().filter(x -> x.getEnd().isInRange()).toList());
        if(!targetAble.isEmpty()){
            targetAble.sort((a, b) -> {
                if (a.turns() > b.turns()) {
                    return 1;
                }
                if (a.turns() < b.turns()) {
                    return -1;
                }
                if(a.getNodes().size() > b.getNodes().size()){
                    return -1;
                }
                if(a.getNodes().size() < b.getNodes().size()){
                    return 1;
                }
                return 0;
            });

            return targetAble.get(0);
        }
        paths.sort((a, b) -> {
            if (a.turns() > b.turns()) {
                return 1;
            }
            if (a.turns() < b.turns()) {
                return -1;
            }
            return 0;
        });

        return paths.get(0);
    }

    public List<GO2Path> findPaths(int movement) {

        List<GO2Path> paths = new ArrayList<>();

        List<GO2Node> possibilities = possibilities(movement);
        Collections.sort(possibilities);
        GO2Node current = null;

        for (int nodeIndex = 0; nodeIndex < possibilities.size(); nodeIndex++) {

            GO2Node possible = possibilities.get(nodeIndex);
            possible.setIndex(nodeIndex);

            List<GO2Node> possiblePath = new ArrayList<>();
            if(possible.getHeuristic() == idealRange && (start.getX() == possible.getX() && start.getY() == possible.getY())){
                //stay as it already is the best place so no move needed
                paths.add(new GO2Path(start, possible, target, possiblePath,  idealRange));
                return paths;
            }
            if (possible.getX() == start.getX() && possible.getY() == start.getY()) {
                continue;
            }
            possiblePath  = isAccessible(possible);
            if (possiblePath.isEmpty()) {
                continue;
            }
            // Remove first because is
            // same as start
            possiblePath.remove(0);

            if (possiblePath.isEmpty()) {
                continue;
            }

            // Turn the node into an accessible list
            possible.setAccessible(true);

            if (current == null) {

                current = possible;
                paths.add(new GO2Path(start, possible, target, possiblePath,  idealRange));
                continue;

            }

            if (current.getDistance() < possible.getDistance()) {
                continue;
            }
            current = possible;
            paths.add(new GO2Path(start, possible, target, possiblePath,  idealRange));
            if(paths.stream().filter(x -> x.getEnd().isIdeal()).count() >= 4){
                break;
            }
        }

        BotLogger.log("Found " + paths.size() + " paths in total!");

        if (paths.isEmpty()) {
            return null;
        }

        return paths;

    }

    public List<GO2Node> isAccessible(GO2Node go2Node) {

        GO2AStar aStar = new GO2AStar(25, 25, start, go2Node, 0);
        aStar.setBlocks(blocks);

        return aStar.findPath();

    }

    public List<GO2Node> possibilities(int maxMovement) {

        List<GO2Node> result = new ArrayList<>();

        int startX = start.getX();
        int startY = start.getY();
        GO2NodeProperty prop = new GO2NodeProperty();
        prop.setIdealRange(idealRange);
        prop.setMinRange(minRange);
        prop.setMaxRange(maxRange);
        prop.setTarget(target);
        prop.setMovement(maxMovement);
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {

                GO2Node node = new GO2Node(i, j);

                node.setOriginDistance(Math.abs(node.getX() - start.getX()) + Math.abs(node.getY() - start.getY()));
                if(node.getOriginDistance() > maxMovement || node.getOriginDistance() > 16){
                    //confirmed out of range, next node instead of continue stack up the RAM
                    continue;
                }
                node.setProp(prop);
                node.setDistance(Math.abs(node.getX() - target.getX()) + Math.abs(node.getY() - target.getY()));
                node.setIdeal(node.getDistance() == idealRange);
                node.setInRange(node.getDistance() >= minRange && node.getDistance() <= maxRange);
                node.setNeedTurn((node.getX() != startX && node.getY() == startY) || (node.getX() == startX && node.getY() != startY));
                // System.out.println("Distance: " + node.getDistance() + ", " + minRange + ", " + maxRange);

                node.setHeuristic(Math.sqrt((target.getX() - node.getX()) * (target.getX() - node.getX()) + (target.getY() - node.getY()) * (target.getY() - node.getY())));
                node.setDiffX(Math.abs(node.getX() - target.getX()));
                node.setDiffY(Math.abs(node.getY() - target.getY()));

                result.add(node);

            }
        }

        return result;

    }

}
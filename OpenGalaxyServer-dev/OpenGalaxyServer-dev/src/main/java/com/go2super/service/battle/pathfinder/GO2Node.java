package com.go2super.service.battle.pathfinder;

import com.go2super.service.battle.astar.Node;
import lombok.Data;

@Data
public class GO2Node implements Comparable<GO2Node> {

    private int index;

    private int x;
    private int y;

    private int originDistance;
    private int distance;
    private boolean needTurn;

    private boolean isInRange;

    private boolean isIdeal;

    //private boolean helpInRange;
    private boolean accessible;

    private int diffX;
    private int diffY;
    private double heuristic;

    // A-Star variables
    private int g;
    private int f;
    private int h;

    private boolean isBlock;
    private GO2Node parent;
    private GO2NodeProperty prop;
    public GO2Node(int x, int y) {

        this.x = x;
        this.y = y;
    }

    public void calculateHeuristic(GO2Node finalNode) {

        this.h = Math.abs(finalNode.getX() - getX()) + Math.abs(finalNode.getY() - getY());
    }

    public void setNodeData(GO2Node currentNode, int cost) {

        int gCost = currentNode.getG() + cost;
        setParent(currentNode);
        setG(gCost);
        calculateFinalCost();
    }

    public boolean checkBetterPath(GO2Node currentNode, int cost) {

        int gCost = currentNode.getG() + cost;
        if (gCost < getG()) {
            setNodeData(currentNode, cost);
            return true;
        }
        return false;
    }

    public int getHeuristic(GO2Node finalNode) {

        int heuristic = Math.abs(finalNode.getX() - getX()) + Math.abs(finalNode.getY() - getY());
        return heuristic;
    }

    private void calculateFinalCost() {

        int finalCost = getG() + getH();
        setF(finalCost);
    }

    @Override
    public int compareTo(GO2Node other) {
        var distanceWeight = Math.abs(prop.getIdealRange() - distance);
        var ccDistanceWeight = Math.abs(other.prop.getIdealRange() - other.distance);

        if (isIdeal && !other.isIdeal) {
            return -1;
        }
        if (!isIdeal && other.isIdeal) {
            return 1;
        }

        if(distanceWeight > ccDistanceWeight){
            return 1;
        }

        if(distanceWeight < ccDistanceWeight){
            return -1;
        }

        if(!needTurn && other.isNeedTurn())
            return 1;

        if(needTurn && !other.isNeedTurn())
            return -1;

        return 0;
    }

    /*@Override
    public String toString() {

        return "Node[X=" + x + ", Y=" + y + ", Ideal=" + isIdeal + ", tooClose = " + tooClose() + ", tooFar = " + tooFar() + "]";
    }*/

}
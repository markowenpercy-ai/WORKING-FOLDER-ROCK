package com.go2super.service.battle.astar;

import lombok.Data;

@Data
public class Node {

    private int g;
    private int f;
    private int h;
    private int x;
    private int y;

    private boolean isBlock;
    private Node parent;

    public Node(int x, int y) {

        super();
        this.x = x;
        this.y = y;
    }

    public void calculateHeuristic(Node finalNode) {

        this.h = Math.abs(finalNode.getX() - getX()) + Math.abs(finalNode.getY() - getY());
    }

    public void setNodeData(Node currentNode, int cost) {

        int gCost = currentNode.getG() + cost;
        setParent(currentNode);
        setG(gCost);
        calculateFinalCost();
    }

    public boolean checkBetterPath(Node currentNode, int cost) {

        int gCost = currentNode.getG() + cost;
        if (gCost < getG()) {
            setNodeData(currentNode, cost);
            return true;
        }
        return false;
    }

    public int getHeuristic(Node finalNode) {

        int heuristic = Math.abs(finalNode.getX() - getX()) + Math.abs(finalNode.getY() - getY());
        return heuristic;
    }

    private void calculateFinalCost() {

        int finalCost = getG() + getH();
        setF(finalCost);
    }

    @Override
    public boolean equals(Object arg0) {

        Node other = (Node) arg0;
        return this.getX() == other.getX() && this.getY() == other.getY();
    }

    @Override
    public String toString() {

        return "Node [X=" + x + ", Y=" + y + "]";
    }

}

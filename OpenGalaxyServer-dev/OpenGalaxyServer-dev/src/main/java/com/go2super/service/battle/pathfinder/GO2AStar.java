package com.go2super.service.battle.pathfinder;

import lombok.Data;

import java.util.*;

@Data
public class GO2AStar {

    private static int DEFAULT_HV_COST = 10; // Horizontal - Vertical Cost
    private static int DEFAULT_DIAGONAL_COST = 14;

    private int P;
    private int hvCost;
    private int diagonalCost;

    private GO2Node[][] searchArea;
    private PriorityQueue<GO2Node> openList;
    private Set<GO2Node> closedSet;
    private GO2Node initialNode;
    private GO2Node finalNode;

    public GO2AStar(int rows, int cols, GO2Node initialNode, GO2Node finalNode, int hvCost, int diagonalCost, int P) {

        this.P = P;
        this.hvCost = hvCost;
        this.diagonalCost = diagonalCost;

        setInitialNode(initialNode);
        setFinalNode(finalNode);

        this.searchArea = new GO2Node[rows][cols];
        this.openList = new PriorityQueue<>(Comparator.comparingInt(GO2Node::getH));

        setNodes();
        this.closedSet = new HashSet<>();

    }

    public GO2AStar(int rows, int cols, GO2Node initialNode, GO2Node finalNode, int P) {

        this(rows, cols, initialNode, finalNode, DEFAULT_HV_COST, DEFAULT_DIAGONAL_COST, P);
    }

    private void setNodes() {

        for (int i = 0; i < searchArea.length; i++) {
            for (int j = 0; j < searchArea[0].length; j++) {
                GO2Node node = new GO2Node(i, j);
                node.calculateHeuristic(getFinalNode());
                this.searchArea[i][j] = node;
            }
        }
    }

    public void setBlocks(int[][] blocksArray) {

        for (int i = 0; i < blocksArray.length; i++) {
            int row = blocksArray[i][0];
            int col = blocksArray[i][1];
            setBlock(row, col);
        }
    }

    public List<GO2Node> findPath() {

        openList.add(initialNode);
        while (!isEmpty(openList)) {
            GO2Node currentNode = openList.poll();
            closedSet.add(currentNode);
            if (isFinalNode(currentNode)) {
                return getPath(currentNode);
            } else {
                addAdjacentNodes(currentNode);
            }
        }
        return new ArrayList<>();
    }

    private List<GO2Node> getPath(GO2Node currentNode) {

        List<GO2Node> path = new ArrayList<>();
        path.add(currentNode);
        GO2Node parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }

    private void addAdjacentNodes(GO2Node currentNode) {

        addAdjacentUpperRow(currentNode);
        addAdjacentMiddleRow(currentNode);
        addAdjacentLowerRow(currentNode);
    }

    private void addAdjacentLowerRow(GO2Node currentNode) {

        int row = currentNode.getX();
        int col = currentNode.getY();
        int lowerRow = row + 1;
        if (lowerRow < getSearchArea().length) {
            checkNode(currentNode, col, lowerRow, getHvCost());
        }
    }

    private void addAdjacentMiddleRow(GO2Node currentNode) {

        int row = currentNode.getX();
        int col = currentNode.getY();
        int middleRow = row;
        if (col - 1 >= 0) {
            checkNode(currentNode, col - 1, middleRow, getHvCost());
        }
        if (col + 1 < getSearchArea()[0].length) {
            checkNode(currentNode, col + 1, middleRow, getHvCost());
        }
    }

    private void addAdjacentUpperRow(GO2Node currentNode) {

        int row = currentNode.getX();
        int col = currentNode.getY();
        int upperRow = row - 1;
        if (upperRow >= 0) {
            checkNode(currentNode, col, upperRow, getHvCost());
        }
    }

    private void checkNode(GO2Node currentNode, int col, int row, int cost) {

        GO2Node adjacentNode = getSearchArea()[row][col];
        if (!adjacentNode.isBlock() && !getClosedSet().contains(adjacentNode)) {
            if (!getOpenList().contains(adjacentNode)) {
                adjacentNode.setNodeData(currentNode, cost);
                getOpenList().add(adjacentNode);
            } else {
                boolean changed = adjacentNode.checkBetterPath(currentNode, cost);
                if (changed) {
                    // Remove and Add the changed node, so that the PriorityQueue can sort again its
                    // contents with the modified "finalCost" value of the modified node
                    getOpenList().remove(adjacentNode);
                    getOpenList().add(adjacentNode);
                }
            }
        }
    }

    private boolean isFinalNode(GO2Node currentNode) {

        return currentNode.getHeuristic(finalNode) == P;
        //return currentNode.equals(finalNode);
    }

    private boolean isEmpty(PriorityQueue<GO2Node> openList) {

        return openList.size() == 0;
    }

    private void setBlock(int row, int col) {

        this.searchArea[row][col].setBlock(true);
    }

}
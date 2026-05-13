package com.go2super.service.battle.astar;

import lombok.Data;

import java.util.*;

@Data
public class AStarImproved {

    private static int DEFAULT_HV_COST = 1; // Horizontal - Vertical Cost
    private static int DEFAULT_DIAGONAL_COST = Integer.MAX_VALUE;

    private int P;
    private int hvCost;
    private int diagonalCost;

    private Node[][] searchArea;
    private PriorityQueue<Node> openList;
    private Set<Node> closedSet;
    private Node initialNode;
    private Node finalNode;

    public AStarImproved(int rows, int cols, Node initialNode, Node finalNode, int hvCost, int diagonalCost, int P) {

        this.P = P;
        this.hvCost = hvCost;
        this.diagonalCost = diagonalCost;

        setInitialNode(initialNode);
        setFinalNode(finalNode);

        this.searchArea = new Node[rows][cols];
        this.openList = new PriorityQueue<>(Comparator.comparingInt(Node::getH));

        setNodes();
        this.closedSet = new HashSet<>();

    }

    public AStarImproved(int rows, int cols, Node initialNode, Node finalNode, int P) {

        this(rows, cols, initialNode, finalNode, DEFAULT_HV_COST, DEFAULT_DIAGONAL_COST, P);
    }

    private void setNodes() {

        for (int i = 0; i < searchArea.length; i++) {
            for (int j = 0; j < searchArea[0].length; j++) {
                Node node = new Node(i, j);
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

    public List<Node> findPath() {

        openList.add(initialNode);
        while (!isEmpty(openList)) {
            Node currentNode = openList.poll();
            closedSet.add(currentNode);
            if (isFinalNode(currentNode)) {
                return getPath(currentNode);
            } else {
                addAdjacentNodes(currentNode);
            }
        }
        return new ArrayList<>();
    }

    private List<Node> getPath(Node currentNode) {

        List<Node> path = new ArrayList<>();
        path.add(currentNode);
        Node parent;
        while ((parent = currentNode.getParent()) != null) {
            path.add(0, parent);
            currentNode = parent;
        }
        return path;
    }

    private void addAdjacentNodes(Node currentNode) {

        addAdjacentUpperRow(currentNode);
        addAdjacentMiddleRow(currentNode);
        addAdjacentLowerRow(currentNode);
    }

    private void addAdjacentLowerRow(Node currentNode) {

        int row = currentNode.getX();
        int col = currentNode.getY();
        int lowerRow = row + 1;
        if (lowerRow < getSearchArea().length) {
            if (col - 1 >= 0) {
                // checkNode(currentNode, col - 1, lowerRow, getDiagonalCost()); // Comment this line if diagonal movements are not allowed
            }
            if (col + 1 < getSearchArea()[0].length) {
                // checkNode(currentNode, col + 1, lowerRow, getDiagonalCost()); // Comment this line if diagonal movements are not allowed
            }
            checkNode(currentNode, col, lowerRow, getHvCost());
        }
    }

    private void addAdjacentMiddleRow(Node currentNode) {

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

    private void addAdjacentUpperRow(Node currentNode) {

        int row = currentNode.getX();
        int col = currentNode.getY();
        int upperRow = row - 1;
        if (upperRow >= 0) {
            if (col - 1 >= 0) {
                // checkNode(currentNode, col - 1, upperRow, getDiagonalCost()); // Comment this if diagonal movements are not allowed
            }
            if (col + 1 < getSearchArea()[0].length) {
                // checkNode(currentNode, col + 1, upperRow, getDiagonalCost()); // Comment this if diagonal movements are not allowed
            }
            checkNode(currentNode, col, upperRow, getHvCost());
        }
    }

    private void checkNode(Node currentNode, int col, int row, int cost) {

        Node adjacentNode = getSearchArea()[row][col];
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

    private boolean isFinalNode(Node currentNode) {

        return currentNode.getHeuristic(finalNode) == P;
        //return currentNode.equals(finalNode);
    }

    private boolean isEmpty(PriorityQueue<Node> openList) {

        return openList.size() == 0;
    }

    private void setBlock(int row, int col) {

        this.searchArea[row][col].setBlock(true);
    }

}
package com.go2super.service.battle.pathfinder;

import com.go2super.logger.BotLogger;
import lombok.Data;

import java.util.*;

@Data
public class GO2Path {

    private List<GO2Node> nodes;

    private GO2Node start;
    private GO2Node end;

    private GO2Node objective;
    private GO2Node target;

    private boolean tooFar;
    private boolean tooClose;
    private int ideaRange;
    public GO2Path(GO2Node start, GO2Node end, GO2Node target, List<GO2Node> nodes, int ideaRange) {

        this.nodes = nodes;

        this.start = start;
        this.end = end;
        this.target = target;
        this.tooFar = ideaRange < end.getDistance();
        this.tooClose = ideaRange > end.getDistance();
        this.ideaRange = ideaRange;


    }

    public int turns() {

        int turns = 0;
        GO2Node last = null;

        Boolean alignedX = null;

        for (GO2Node node : nodes) {
            if (last == null) {
                last = node;
            } else if (last.getX() == node.getX()) {
                if (alignedX != null && !alignedX) {
                    turns++;
                }
                last = node;
                alignedX = true;
                continue;
            } else if (last.getY() == node.getY()) {
                if (alignedX != null && alignedX) {
                    turns++;
                }
                last = node;
                alignedX = false;
                continue;
            } else {
                last = node;
                turns++;
            }
        }

        return turns;

    }
}
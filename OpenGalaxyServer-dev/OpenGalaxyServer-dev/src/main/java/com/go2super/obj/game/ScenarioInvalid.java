package com.go2super.obj.game;

import com.go2super.obj.utility.GameCell;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ScenarioInvalid {

    private List<GameCell> invalidCells = new ArrayList<>();

    public boolean isInvalid(int x, int y) {

        for (GameCell cell : invalidCells) {
            if (cell.getX() == x && cell.getY() == y) {
                return true;
            }
        }
        return false;
    }

    public ScenarioInvalid add(int x, int y) {

        invalidCells.add(GameCell.of(x, y));
        return this;
    }

    public ScenarioInvalid build() {

        return this;
    }

    public static ScenarioInvalid builder() {

        return new ScenarioInvalid();
    }

}

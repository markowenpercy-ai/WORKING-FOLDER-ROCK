package com.go2super.obj.utility;

import com.go2super.socket.util.MathUtil;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameRegion {

    private GameCell min;
    private GameCell max;

    public GameCell randomCell() {

        return GameCell.of(MathUtil.random(min.getX(), max.getX()), MathUtil.random(min.getY(), max.getY()));
    }

    public boolean isInside(int x, int y) {

        return x >= min.getX() && y >= min.getY() && x <= max.getX() && y <= max.getY();
    }

    public static GameRegion of(GameCell min, GameCell max) {

        return new GameRegion(min, max);
    }

}

package com.go2super.obj.utility;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class GameCell {

    private int x;
    private int y;

    public GalaxyTile getTile() {

        return new GalaxyTile(x, y);
    }

    public static GameCell of(int x, int y) {

        return new GameCell(x, y);
    }

}

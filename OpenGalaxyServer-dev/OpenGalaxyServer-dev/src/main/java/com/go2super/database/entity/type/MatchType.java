package com.go2super.database.entity.type;

import lombok.Getter;

import java.io.Serializable;
import java.util.List;

public enum MatchType implements Serializable {
    INSTANCE_MATCH(0, true),
    ARENA_MATCH(1, true),
    RAIDS_MATCH(2, true),
    LEAGUE_MATCH(3, true),
    CHAMPION_MATCH(4, true),
    IGL_MATCH(5, true),
    PVP_MATCH(6, false),
    RBP_MATCH(7, false),
    HUMAROID_MATCH(8, false),
    PIRATES_MATCH(9, false);

    @Getter
    private final int code;

    @Getter
    private final boolean virtual;

    MatchType(int code, boolean virtual) {

        this.code = code;
        this.virtual = virtual;
    }

    public static List<MatchType> getAllNonVirtual() {
        return List.of(PVP_MATCH, RBP_MATCH, HUMAROID_MATCH, PIRATES_MATCH);
    }

}

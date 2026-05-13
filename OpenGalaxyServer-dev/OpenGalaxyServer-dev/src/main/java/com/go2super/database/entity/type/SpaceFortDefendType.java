package com.go2super.database.entity.type;

import lombok.Getter;

public enum SpaceFortDefendType {

    NONE(0),
    COMMON_DEFEND(6),

    ;

    @Getter
    private final int defendType;

    SpaceFortDefendType(int defendType) {

        this.defendType = defendType;
    }

    public static SpaceFortDefendType getByCode(int code) {

        for (SpaceFortDefendType type : SpaceFortDefendType.values()) {
            if (type.getDefendType() == code) {
                return type;
            }
        }
        return NONE;
    }

}

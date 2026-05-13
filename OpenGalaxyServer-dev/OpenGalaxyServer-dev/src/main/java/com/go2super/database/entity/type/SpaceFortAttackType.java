package com.go2super.database.entity.type;

import lombok.Getter;

public enum SpaceFortAttackType {

    NONE(0),
    SINGLE_TARGET(1),
    RADIUS_TARGET(2),
    ALL_ATTACK(3);

    @Getter
    private final int attackType;

    SpaceFortAttackType(int attackType) {

        this.attackType = attackType;
    }

    public static SpaceFortAttackType getByCode(int code) {

        for (SpaceFortAttackType type : SpaceFortAttackType.values()) {
            if (type.getAttackType() == code) {
                return type;
            }
        }
        return NONE;
    }

}

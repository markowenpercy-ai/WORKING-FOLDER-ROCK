package com.go2super.database.entity.type;

import lombok.Getter;

import java.io.Serializable;

public enum ExpertiseType implements Serializable {
    S(4),
    A(3),
    B(2),
    C(1),
    D(0);

    @Getter
    private final int value;

    ExpertiseType(int value) {

        this.value = value;
    }

    public boolean isBetterThan(ExpertiseType other) {

        return this.value > other.value;
    }

    public static ExpertiseType getLiteral(String c) {

        for (ExpertiseType type : ExpertiseType.values()) {
            if (c.equalsIgnoreCase(type.name())) {
                return type;
            }
        }
        return ExpertiseType.D;
    }

    public static ExpertiseType getLiteral(char c) {

        for (ExpertiseType type : ExpertiseType.values()) {
            if (String.valueOf(c).equalsIgnoreCase(type.name())) {
                return type;
            }
        }
        return ExpertiseType.D;
    }

}

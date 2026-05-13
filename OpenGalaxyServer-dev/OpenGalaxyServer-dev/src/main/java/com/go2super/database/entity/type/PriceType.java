package com.go2super.database.entity.type;

import lombok.Getter;

public enum PriceType {
    GOLD(0),
    MP(1);

    @Getter
    private final int code;

    PriceType(int code) {

        this.code = code;
    }

    public static PriceType getByCode(int code) {

        for (PriceType priceType : PriceType.values()) {
            if (priceType.getCode() == code) {
                return priceType;
            }
        }
        return null;
    }

}

package com.go2super.database.entity.type;

import lombok.Getter;

public enum TradeType {
    ALL(-1),
    SHIP(0),
    ITEM(1),
    BLUEPRINT(2),
    CARD(3),
    GEM(4);

    @Getter
    private final int code;

    TradeType(int code) {

        this.code = code;
    }

    public static TradeType getByCode(int code) {

        for (TradeType tradeType : TradeType.values()) {
            if (tradeType.getCode() == code) {
                return tradeType;
            }
        }
        return null;
    }

}

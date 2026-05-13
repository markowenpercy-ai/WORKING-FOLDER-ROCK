package com.go2super.database.entity.type;

import lombok.Getter;

public enum PlanetType {
    USER_PLANET(0, 0),
    HUMAROID_PLANET(2, 3),
    RESOURCES_PLANET(3, 1);

    @Getter
    private final int code;
    @Getter
    private final int msgId;

    PlanetType(int code, int msgId) {

        this.code = code;
        this.msgId = msgId;
    }

}

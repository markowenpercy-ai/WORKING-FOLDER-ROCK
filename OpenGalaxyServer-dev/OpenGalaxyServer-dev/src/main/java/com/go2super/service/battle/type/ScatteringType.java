package com.go2super.service.battle.type;

public enum ScatteringType {

    NONE,
    GLOBAL,
    VERTICAL,
    HORIZONTAL,
    DISTRIBUTED;

    public static ScatteringType fromAttackType(String weaponType) {

        switch (weaponType) {
            case "ballistic":
                return HORIZONTAL;
            case "directional":
                return VERTICAL;
            case "missile":
                return GLOBAL;
            default:
                return NONE;
        }
    }

}
package com.go2super.obj.type;

public enum TransitionType {

    HOME_FLEETS_IN_TRANSITION(0),
    DEFENSIVE_HOME_FLEETS_IN_TRANSITION(1),
    AGGRESSIVE_ENEMY_FLEETS_IN_TRANSITION(2),

    HOME_FLEETS_IN_BATTLE_MODE(3),
    HOME_FLEETS_IN_DEFENSE_MODE(4),

    ;

    private final int gameCode;

    TransitionType(int gameCode) {

        this.gameCode = gameCode;
    }

    public int getGameCode() {

        return this.gameCode;
    }

}

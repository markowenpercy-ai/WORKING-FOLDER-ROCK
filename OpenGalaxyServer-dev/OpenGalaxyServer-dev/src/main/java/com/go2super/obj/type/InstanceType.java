package com.go2super.obj.type;

public enum InstanceType {

    INSTANCE(true),
    RESTRICTED(true),
    TRIALS(false),
    CONSTELLATION(true);

    private final boolean playerFormation;

    InstanceType(boolean playerFormation) {

        this.playerFormation = playerFormation;
    }

    public boolean hasPlayerFormation() {

        return playerFormation;
    }

}

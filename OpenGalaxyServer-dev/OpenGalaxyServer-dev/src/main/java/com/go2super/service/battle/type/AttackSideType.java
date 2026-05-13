package com.go2super.service.battle.type;

import lombok.Getter;

public enum AttackSideType {

    NONE(false),
    DEFENDER(false),
    ATTACKER(true);

    @Getter
    private final boolean attacker;

    AttackSideType(boolean attacker) {

        this.attacker = attacker;
    }

}
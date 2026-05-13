package com.go2super.obj.type;

import lombok.Getter;

public enum MailType {

    BOUGHT(0),
    SOLD(1),

    IGL(22),

    BATTLE_REWARD(4),

    LEAGUE_REPORT(14)

    ;

    @Getter
    private final int titleType;

    MailType(int titleType) {

        this.titleType = titleType;
    }

}

package com.go2super.database.entity.sub.battle;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class BattleActionTrigger {

    private int id;
    private String type;

    private long millis;

}

package com.go2super.database.entity.sub.battle.trigger;

import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class FleetRotateTrigger extends BattleActionTrigger {

    private int guid;
    private int shipTeamId;
    private int direction;

}

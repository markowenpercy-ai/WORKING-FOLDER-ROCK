package com.go2super.database.entity.sub.battle.trigger;

import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.database.entity.type.BattleElementType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class FleetMarkTargetTrigger extends BattleActionTrigger {

    private BattleElementType elementType;

    private int attackerShipTeamId;
    private int targetId;

}

package com.go2super.database.entity.sub.battle.trigger;

import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.service.BattleService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class MovementTrigger extends BattleActionTrigger {

    private int shipTeamId;
    private int movementId;

    private int fromX;
    private int fromY;

    private int toX;
    private int toY;

    private boolean sourceAnimation = false;

    public int calculatePathMovement() {

        return BattleService.getDirection(fromX, fromY, toX, toY);
    }

}

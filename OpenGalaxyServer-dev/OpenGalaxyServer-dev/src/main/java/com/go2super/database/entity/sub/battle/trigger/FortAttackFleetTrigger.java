package com.go2super.database.entity.sub.battle.trigger;

import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.database.entity.sub.battle.meta.FortCellAttackMeta;
import com.go2super.service.battle.calculator.ShipAttackPacketAction;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;

import java.util.*;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class FortAttackFleetTrigger extends BattleActionTrigger {

    private int attackerFortId;
    private List<FortCellAttackMeta> attacks;

    @Transient
    private List<ShipAttackPacketAction> shipAttackPacketActions = new ArrayList<>();

    private double nextAttack;

    public FortCellAttackMeta getMeta(int fromFortId) {

        for (FortCellAttackMeta attackMeta : attacks) {
            if (attackMeta.getFromFortId() == fromFortId) {
                return attackMeta;
            }
        }

        return null;

    }

}

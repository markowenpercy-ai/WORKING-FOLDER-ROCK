package com.go2super.database.entity.sub.battle.trigger;

import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.database.entity.sub.battle.meta.AssaultCellAttackMeta;
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
public class FleetAttackFortTrigger extends BattleActionTrigger {

    private int attackerShipTeamId;
    private int defenderFortId; // * add

    private List<AssaultCellAttackMeta> attacks;

    @Transient
    private List<ShipAttackPacketAction> shipAttackPacketActions = new ArrayList<>();

    public AssaultCellAttackMeta getMeta(int fromId) {

        for (AssaultCellAttackMeta attackMeta : attacks) {
            if (attackMeta.getFromId() == fromId) {
                return attackMeta;
            }
        }

        return null;

    }

}

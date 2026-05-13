package com.go2super.service.battle.calculator;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.service.battle.BattleFleetCell;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipShootdowns {

    private BattleFleet attacker;
    private BattleFleetCell attackerCell;

    private int amount;

}

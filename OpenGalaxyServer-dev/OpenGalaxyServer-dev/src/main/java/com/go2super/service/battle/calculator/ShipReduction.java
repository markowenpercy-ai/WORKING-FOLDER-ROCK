package com.go2super.service.battle.calculator;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.sub.BattleFleet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipReduction {

    private ShipPosition position;

    private double supplyReduction;
    private double storageReduction;

    private double shieldsReduction;
    private double structureReduction;

    private double reflectionReduction;
    private double amountReduction;
}

package com.go2super.service.battle.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipHighestAttack {

    private int attacker;
    private int attackerCell;

    private int highestAttack;

}

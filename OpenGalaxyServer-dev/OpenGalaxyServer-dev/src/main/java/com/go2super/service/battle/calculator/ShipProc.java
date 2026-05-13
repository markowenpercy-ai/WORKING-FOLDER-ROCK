package com.go2super.service.battle.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipProc {

    private double rate;
    private int round;

    private boolean triggered;

}

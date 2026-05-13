package com.go2super.service.battle.calculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleUsage {

    private int moduleIndex;

    private double fuelUsage;
    private int effectiveStack;

    private int reload;
    private int lastShoot;

}

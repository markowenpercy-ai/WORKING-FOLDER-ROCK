package com.go2super.service.battle.calculator;

import com.go2super.service.battle.module.BattleFleetAttackModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipAttack {

    private String weaponSubType;
    private String damageType;

    private List<BattleFleetAttackModule> attackModules;

    // Can only intercept missile
    // and shipBased
    public boolean canBeIntercepted() {

        return weaponSubType.equals("missile") || weaponSubType.equals("shipBased");
    }

    public boolean isBallistic() {

        return weaponSubType.equals("ballistic");
    }

    public boolean isDirectional() {

        return weaponSubType.equals("directional");
    }

    public boolean isShipBased() {

        return weaponSubType.equals("shipBased");
    }

    public boolean isMissile() {

        return weaponSubType.equals("missile");
    }

}

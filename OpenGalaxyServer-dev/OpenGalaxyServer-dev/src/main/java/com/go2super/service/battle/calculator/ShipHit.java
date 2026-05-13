package com.go2super.service.battle.calculator;

import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipHit {

    @Transient
    private BattleFleetCell battleFleetCell;

    private int moduleId = -1;
    private List<BattleFleetAttackModule> attackModules;

    public BattleFleetAttackModule getReference() {

        return attackModules.get(0);
    }

}

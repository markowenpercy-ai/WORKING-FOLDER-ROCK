package com.go2super.service.battle.calculator;

import com.go2super.service.battle.type.EffectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetEffects implements Serializable {

    private int shipTeamId = -1;
    private List<FleetEffect> effects = new ArrayList<>();

    public FleetEffect getEffect(EffectType effectType) {

        return effects.stream().filter(shipEffect -> shipEffect.getEffectType() == effectType).findFirst().orElse(null);
    }

    public boolean contains(EffectType... effectType) {

        for (EffectType effect : effectType) {
            if (effects.stream().anyMatch(shipEffect -> shipEffect.getEffectType() == effect)) {
                return true;
            }
        }
        return false;
    }

}

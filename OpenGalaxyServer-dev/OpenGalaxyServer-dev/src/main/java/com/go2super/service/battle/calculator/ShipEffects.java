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
public class ShipEffects implements Serializable {

    private List<ShipEffect> effects = new ArrayList<>();

    public ShipEffect getEffect(EffectType effectType) {

        return effects.stream().filter(shipEffect -> shipEffect.getEffectType() == effectType).findFirst().orElse(null);
    }

    public boolean contains(EffectType effectType) {

        return effects.stream().anyMatch(shipEffect -> shipEffect.getEffectType() == effectType);
    }

}

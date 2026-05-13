package com.go2super.service.battle.calculator;

import com.go2super.service.battle.type.EffectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipEffect implements Serializable {

    private boolean remove;
    private EffectType effectType;

    private double value;
    private int until;

}

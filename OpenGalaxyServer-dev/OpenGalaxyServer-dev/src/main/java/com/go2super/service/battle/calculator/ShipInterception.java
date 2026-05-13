package com.go2super.service.battle.calculator;

import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipInterception {

    private double interceptionRate;
    private List<BattleFleetDefensiveModule> interceptors = new ArrayList<>();

    private Set<ShipHit> intercepted = new HashSet<>();

    public void addUsage(ShipUsage usage) {

        for (BattleFleetDefensiveModule module : interceptors) {
            usage.add(module, 0, 0);
        }
    }

    public double getFuelUsage(int effectiveStack) {

        return (getReference().getFuelUsage() * (double) interceptors.size()) * (double) effectiveStack;
    }

    public BattleFleetDefensiveModule getReference() {

        return interceptors.get(0);
    }

    public void add(List<BattleFleetDefensiveModule> defensiveModules, double interceptionRate) {

        this.interceptionRate = interceptionRate;
        this.interceptors = defensiveModules;

    }

    public boolean has(ShipHit shipHit) {

        return intercepted.contains(shipHit);
    }

    public void add(ShipHit shipHit) {

        intercepted.add(shipHit);
    }

}

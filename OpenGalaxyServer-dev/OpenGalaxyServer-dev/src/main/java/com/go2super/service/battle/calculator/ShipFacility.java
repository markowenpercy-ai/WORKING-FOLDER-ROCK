package com.go2super.service.battle.calculator;

import com.go2super.logger.BotLogger;
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
public class ShipFacility {

    private List<BattleFleetDefensiveModule> facility = new ArrayList<>();

    private int moduleId = -1;
    private boolean enabled = false;

    public void trigger(int round) {

        enabled = true;
        for (BattleFleetDefensiveModule module : facility) {
            module.setLastShoot(round);
        }
    }

    public boolean isReloaded(int roundId) {

        BattleFleetDefensiveModule reference = reference();
        return reference != null && (reference.getLastShoot() + reference.getCooldown()) < roundId;
    }

    public double getEffect(String effectName) {

        return (Double) reference().getMeta().getEffect(effectName).getValue();
    }

    public BattleFleetDefensiveModule reference() {

        return facility.isEmpty() ? null : facility.get(0);
    }

    public void addUsage(ShipUsage usage) {

        for (BattleFleetDefensiveModule module : facility) {
            usage.add(module, 0, 0);
        }
    }

    public double getFuelUsage(int effectiveStack) {

        return (reference().getFuelUsage() * (double) facility.size()) * (double) effectiveStack;
    }

    public double getFuelUsage() {

        BattleFleetDefensiveModule reference = reference();
        return reference == null ? 0 : reference.getFuelUsage() * facility.size();
    }

    public void add(BattleFleetDefensiveModule defensive, String damageType) {

        if (moduleId == -1) {

            moduleId = defensive.getModuleId();

        } else if (moduleId != defensive.getModuleId()) {

            BotLogger.error("Invalid facility for defensive: " + defensive.getModuleId() + "!");
            return;

        }

        facility.add(defensive);

    }

}

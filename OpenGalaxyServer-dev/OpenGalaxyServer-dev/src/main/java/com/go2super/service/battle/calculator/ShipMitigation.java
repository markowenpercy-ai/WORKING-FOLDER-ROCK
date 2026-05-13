package com.go2super.service.battle.calculator;

import com.go2super.logger.BotLogger;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipMitigation {

    private List<BattleFleetDefensiveModule> mitigation = new ArrayList<>();

    private int moduleId = -1;

    private double absorbGeneral;
    private double absorbExplosive;
    private double absorbMagnetic;
    private double absorbKinetic;
    private double absorbHeat;

    public int count() {

        return mitigation == null ? 0 : mitigation.size();
    }

    public double getTotalMitigation(String subType) {

        double result = absorbGeneral;

        switch (subType) {

            case "explosive":
                result += absorbExplosive;
                break;
            case "magnetic":
                result += absorbMagnetic;
                break;
            case "kinetic":
                result += absorbKinetic;
                break;
            case "heat":
                result += absorbHeat;
                break;

        }

        return result;
    }

    public void addUsage(ShipUsage usage) {

        for (BattleFleetDefensiveModule module : mitigation) {
            usage.add(module, 0, 0);
        }
    }

    public double getFuelUsage(int effectiveStack) {

        return (getReference().getFuelUsage() * (double) mitigation.size()) * (double) effectiveStack;
    }

    public double getFuelUsage() {

        BattleFleetDefensiveModule reference = getReference();
        return reference == null ? 0 : reference.getFuelUsage() * mitigation.size();
    }

    public BattleFleetDefensiveModule getReference() {

        if (mitigation.isEmpty()) {
            return null;
        }
        return mitigation.iterator().next();
    }

    public void add(BattleFleetDefensiveModule defensive, double general, List<BattleFleetAttackModule> attackModules, double custom) {

        if (moduleId == -1) {

            moduleId = defensive.getModuleId();

        } else if (moduleId != defensive.getModuleId()) {

            BotLogger.error("Invalid mitigation for defensive: " + defensive.getModuleId() + "!");
            return;

        }
        var optionalAttack = attackModules.stream().map(x -> x.getDamageType())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
                .max(Map.Entry.comparingByValue());
        var damageType = "fort";
        if(optionalAttack.isPresent()){
            damageType = optionalAttack.get().getKey();
        }
        mitigation.add(defensive);

        switch (damageType) {
            case "explosive":
                absorbExplosive += custom;
                break;
            case "magnetic":
                absorbMagnetic += custom;
                break;
            case "kinetic":
                absorbKinetic += custom;
                break;
            case "heat":
                absorbHeat += custom;
                break;
        }

        absorbGeneral += general;

    }

}

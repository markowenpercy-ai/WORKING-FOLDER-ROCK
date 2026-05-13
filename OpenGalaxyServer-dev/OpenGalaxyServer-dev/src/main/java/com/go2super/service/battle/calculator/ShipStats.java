package com.go2super.service.battle.calculator;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipStats {

    // Model
    private ShipModel shipModel;

    // Common effects
    private String armorType;
    private String bodyType;

    private double agility;
    private double steering;
    private double stability;
    private double daedalus;

    // Percent effects
    private double criticalRate;
    private double doubleRate;
    private boolean flankIgnore;
    private double flankRate;
    private double damageReduce;
    private double hitRate;
    private double pierceShield;
    private double damageBonus;
    private double criticalRateReduce;

    // Bonuses
    private double hitRateBonus;
    private double stabilityBonus;
    private double criticalRateBonus;

    // Flagship
    private double allShieldHeal;
    private double allStabilityReduce;
    private double allPierceShield;
    private double allAttackSplash;
    private double allMovementBonus;
    private double allCritRate;
    private double allDamageReduce;
    private double allReflectBonus;
    private double allDoubleRate;
    private double allNegatePartBonus;
    private Map<String, Double> allArmorBonus;

    private Map<String, Double> armorBonus;

    private Map<String, Double> partBonus;

    public void pass(BattleFleet fleet) {

        // Find flagship
        Optional<ShipModel> optionalFlagshipModel = fleet.getFlagship();
        if (optionalFlagshipModel.isEmpty()) {
            return;
        }

        ShipModel flagshipModel = optionalFlagshipModel.get();
        BodyLevelMeta levelMeta = flagshipModel.getBodyLevelMeta();

        // Add flagship bonuses

        // Effect: allShieldHeal
        // ~ Intrepid Nexus Passive
        allShieldHeal = levelMeta.getEffectValueAsDouble("allShieldHeal");

        // Effect: allStabilityReduce
        // ~ Grim Reaper Passive
        // allStabilityReduce = levelMeta.getEffectValueAsDouble("allStabilityReduce");

        // Effect: allPierceShield
        // ~ Shadow Trojan Passive
        allPierceShield = levelMeta.getEffectValueAsDouble("allPierceShield");

        // Effect: allAttackSplash
        // ~ Conquistador Passive
        allAttackSplash = levelMeta.getEffectValueAsDouble("allAttackSplash");

        // Effect: allMovementBonus
        // ~ Mercury Wing Passive
        allMovementBonus = levelMeta.getEffectValueAsDouble("allMovementBonus");

        // Effect: allCritRate
        // ~ Firecat Passive
        allCritRate = levelMeta.getEffectValueAsDouble("allCritRate");

        // Effect: allDamageReduce
        // ~ GForce's Dreadnaught Passive
        allDamageReduce = levelMeta.getEffectValueAsDouble("allDamageReduce");

        // Effect: allReflectBonus
        // ~ Arbiter Passive
        allReflectBonus = levelMeta.getEffectValueAsDouble("allReflectBonus");

        // Effect: allDoubleRate
        // ~ GFS-Vengeance Passive
        allDoubleRate = levelMeta.getEffectValueAsDouble("allDoubleRate");

        // Effect: allDoubleRate
        // ~ GFS-Vengeance Passive
        allDoubleRate = levelMeta.getEffectValueAsDouble("allDoubleRate");

        // Effect: allNegatePartBonus
        // ~ Zefram-MK42 Passive
        allNegatePartBonus = levelMeta.getEffectValueAsDouble("allNegatePartBonus");

        // Effect: allArmorBonus
        // ~ Kazati Passive
        allArmorBonus = flagshipModel.getArmorBonus();
        addArmorBonus(allArmorBonus);

    }

    public void pass(ShipModel shipModel) {

        this.shipModel = shipModel;

        // Body related attributes
        ShipBodyData body = ResourceManager.getShipBodies().findByBodyId(shipModel.getBodyId());

        this.armorType = body.getArmorType();
        this.bodyType = body.getBodyType();

        // Add basic attributes
        addAgility(shipModel.getAgility());
        addSteering(shipModel.getSteeringBonus());
        addStability(shipModel.getStability());
        addDaedalus(shipModel.getDaedalus());

        // Add percent effects
        addCriticalRate(shipModel.getCritRate());
        addDoubleRate(shipModel.getDoubleRate());
        addFlankIgnore(shipModel.getFlankIgnore());
        addDamageReduce(shipModel.getDamageReduce());
        addFlankRate(shipModel.getFlankRate());
        addHitRate(shipModel.getHitRate());
        addPierceShield(shipModel.getPierceShield());
        addDamageBonus(shipModel.getDamageBonus());
        addCriticalRateReduce(shipModel.getCriticalRateReduce());

        // Add bonuses
        addHitRateBonus(shipModel.getHitRateBonus());
        addStabilityBonus(shipModel.getStabilityBonus());
        addCriticalRateBonus(shipModel.getCritRateBonus());
        addPartBonus(shipModel.getPartBonus());

        // Map bonuses
        addArmorBonus(shipModel.getArmorBonus());

    }

    private void addPartBonus(Map<String, Double> partBonus) {
        this.partBonus = partBonus;
    }

    private void addCriticalRateReduce(double critRateReduce) {
        this.criticalRateReduce += critRateReduce;
    }

    private void addFlankIgnore(boolean flankIgnore) {
       this.flankIgnore = flankIgnore;
    }

    private void addFlankRate(double flankRate) {
        this.flankRate += flankRate;
    }

    private void addDamageReduce(double damageReduce) {
        this.damageReduce += damageReduce;
    }

    private void addHitRate(double hitRate) {
        this.hitRate += hitRate;
    }

    private void addPierceShield(double pierceShield) {
        this.pierceShield += pierceShield;
    }

    private void addDamageBonus(double damageBonus) {
        this.damageBonus += damageBonus;
    }

    public void addDoubleRate(double value) {

        this.doubleRate += value;
    }

    public void addCriticalRateBonus(double value) {

        this.criticalRateBonus += value;
    }

    public void addCriticalRate(double value) {

        this.criticalRate += value;
    }

    public void addAgility(double value) {

        this.agility += value;
    }

    public void addSteering(double value) {

        this.steering += value;
    }

    public void addStability(double value) {

        this.stability += value;
    }

    public void addStabilityBonus(double value) {

        this.stabilityBonus += value;
    }

    public void addDaedalus(double value) {

        this.daedalus += value;
    }

    public void addHitRateBonus(double value) {

        this.hitRateBonus += value;
    }

    public void addArmorBonus(Map<String, Double> armorBonus) {

        if (this.armorBonus == null) {
            this.armorBonus = armorBonus;
            return;
        }

        for (Map.Entry<String, Double> entry : armorBonus.entrySet()) {
            String key = entry.getKey();
            Double value = entry.getValue();
            if (this.armorBonus.containsKey(key)) {
                this.armorBonus.put(key, this.armorBonus.get(key) + value);
            } else {
                this.armorBonus.put(key, value);
            }
        }

    }


}

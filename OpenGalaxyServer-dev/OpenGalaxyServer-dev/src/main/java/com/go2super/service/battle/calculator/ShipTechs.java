package com.go2super.service.battle.calculator;

import com.go2super.database.entity.sub.UserTech;
import com.go2super.resources.data.ResearchData;
import com.go2super.resources.data.meta.ResearchEffectMeta;
import com.go2super.resources.data.meta.ResearchLevelMeta;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class ShipTechs implements Serializable {

    private List<UserTech> techs;

    // Science [Laser]

    private double laserDamageBonus;
    private double laserCriticalRateBonus;
    private double laserHitRateBonus;
    private double laserSteeringPowerBonus;
    private double laserPenetration;
    private double laserPiercing;

    private boolean laserPiercedCriticalStrike;

    private double laserDecreaseTargetDamage;
    private double laserDecreaseTargetDamageRounds;
    private double laserMovementCriticalRateBonus;

    private double laserRadiativeInterference;
    private double laserRadiativeInterferenceMaximum;

    private double laserReduceSteeringPowerChance;
    private double laserReduceSteeringPowerRate;

    private double laserIncreaseMaximumHitRateChance;
    private double laserIgnoreAgilityChance;
    private double laserIgnoreAgilityRate;

    private double laserReduceDefenseRateBonus;
    private double laserFleetMovementReductionChance;
    private double laserFleetMovementReductionAmount;
    private double laserIncreaseRangeAmount;

    // Science [Ballistic]

    private double ballisticDamageBonus;
    private double ballisticCriticalRateBonus;
    private double ballisticCriticalDamageBonus;
    private double ballisticHitRateBonus;
    private double ballisticScattering;
    private double ballisticPenetration;

    private double ballisticKineticDamageAgainstNeutralizingRate;
    private double ballisticKineticDamageAgainstLightRate;
    private double ballisticKineticDamageAgainstUnknownRate;

    private double ballisticHeatDamageAgainstRegenRate;
    private double ballisticHeatDamageAgainstLightRate;
    private double ballisticHeatDamageAgainstUnknownRate;

    private double ballisticDamageAgainstLightRate;
    private double ballisticDamageAgainstUnknownRate;

    private double ballisticRange1IncreaseDamageRateBonus;
    private double ballisticRange2IncreaseDamageRateBonus;
    private double ballisticRange3IncreaseDamageRateBonus;
    private double ballisticRange4IncreaseDamageRateBonus;

    private double ballisticIncreaseDispersionMaximumChance;
    private double ballisticIncreaseDispersionMaximumDamageRateBonus;

    private double ballisticDamageOnHpHit;
    private double ballisticDamageOnHpOrLess;
    private double ballisticIncreaseRange;

    private double ballisticDamageAgainstRegenRate;
    private double ballisticDamageAgainstChromeRate;
    private double ballisticDamageAgainstNanoRate;
    private double ballisticDamageAgainstNeutralizingRate;

    // Science [Missile]

    private double missileDamageBonus;
    private double missileHitRateBonus;
    private double missilePenetration;
    private double missileSteeringPowerBonus;
    private double missileScattering;
    private double missileInterceptRateBonus;

    private double missileLowerStructureScattering;
    private double missileHE3ReductionRateBonus;

    private double missileCriticalRateBonus;
    private double missileHigherStructureScattering;

    private double missileAdditionalIncreaseDamageChance;
    private double missileAdditionalIncreaseDamageValue;

    private double missileAdditionalHE3ReductionChance;
    private double missileAdditionalHE3ReductionValue;

    private double missileRepelChance;
    private double missileRepelRange;
    private double missileCooldownReduction;
    private double missileIncreaseRange;

    // Science [Ship-Based]

    private double shipBasedDamageBonus;
    private double shipBasedHitRateBonus;
    private double shipBasedInterceptRateBonus;
    private double shipBasedSteeringPowerBonus;

    private double shipBasedHE3ReductionRateBonus;

    private double shipBasedLongRangedAway6SlotsIncreaseBaseDamage;
    private double shipBasedLongRangedAway7SlotsIncreaseBaseDamage;
    private double shipBasedLongRangedAway8SlotsIncreaseBaseDamage;
    private double shipBasedLongRangedAway9SlotsIncreaseBaseDamage;
    private double shipBasedLongRangedAway10SlotsIncreaseBaseDamage;

    private double shipBasedCriticalDamageBonus;

    private double shipBasedImproveHitChance;
    private double shipBasedImproveHitDamageBonus;
    private double shipBasedImproveHitHe3Cost;

    private double shipBasedInstantReloadChance;
    private double shipBasedCriticalRateBonus;
    private double shipBasedLongRangedAwayDoubleChance;

    private double shipBasedCustomInterceptRate;
    private double shipBasedIncreaseRange;
    private double shipBasedCooldownReduction;

    private double shipBasedEnemyNoShieldIncreaseDamageBonus;
    private double shipBasedEnemyWithShieldIncreaseDamageBonus;

    // Science [Ship Defence]

    private double shipShieldBonus;
    private double shipArmorBonus;
    private double shipAgilityBonus;
    private double shipDaedalusBonus;
    private double shipStabilityBonus;

    private double shieldModuleDamageReduction;
    private double enemyCriticalStrikeDamageReduction;
    private double enemyShieldPenetrationReductionRateBonus;
    private double reactionArmorDamageReductionBonus;

    private double armorTypeChromeDamageReductionBonus;
    private double armorTypeNanoDamageReductionBonus;
    private double armorTypeNeutralizingDamageReductionBonus;
    private double armorTypeRegenDamageReductionBonus;
    private double armorTypeLightDamageReductionBonus;
    private double armorTypeUnknownDamageReductionBonus;

    private double allNegationModulesNoHe3ConsumptionRateBonus;
    private double enableReflectModulesReactivationAfterRounds = -10000;

    private double shieldHealRateBonus;
    private double armorHealRateBonus;

    private double interceptModuleRateBonus;
    private double beforeShieldDamageReflectionRateBonus;
    private double moduleDoubleDamageAbsorptionChanceBonus;
    private double scatteringDamageReductionRateBonus;

    private double activatedDaedalusShipStabilityAugmentChance;
    private double activatedDaedalusShipStabilityAugment;
    private double activatedDaedalusShipDamageReduction;

    public ShipTechs() {

    }

    public ShipTechs(List<UserTech> techs) {

        passUser(techs);
    }

    public void passData(List<ResearchData> techs) {

        // * Defaults
        this.techs = new ArrayList<>();

        for (ResearchData data : techs) {

            ResearchLevelMeta levelMeta = data.getLevel(data.getLevels().size() - 1);
            if (levelMeta == null) {
                continue;
            }

            pass(levelMeta);

        }

    }

    public void passUser(List<UserTech> techs) {

        // * Defaults
        this.techs = techs;

        for (UserTech tech : techs) {

            ResearchLevelMeta levelMeta = tech.getLevelMeta();
            if (levelMeta == null) {
                continue;
            }

            pass(levelMeta);

        }

    }

    public double getAttackInterceptRateBonus(String subType) {

        return fetchBonus(subType, 0.0d, 0.0d, missileInterceptRateBonus, shipBasedInterceptRateBonus);
    }

    public double getScattering(String subType) {

        return fetchBonus(subType, ballisticScattering, laserPiercing, missileScattering, 0.0d);
    }

    public double getPenetration(String subType) {

        return fetchBonus(subType, ballisticPenetration, laserPenetration, missilePenetration, 0.0d);
    }

    public double getSteeringPowerBonus(String subType) {

        return fetchBonus(subType, 0.0d, laserSteeringPowerBonus, missileSteeringPowerBonus, shipBasedSteeringPowerBonus);
    }

    public double getHitRateBonus(String subType) {

        return fetchBonus(subType, ballisticHitRateBonus, laserHitRateBonus, missileHitRateBonus, shipBasedHitRateBonus);
    }

    public double getCriticalRateBonus(String subType) {

        return fetchBonus(subType, ballisticCriticalRateBonus, laserCriticalRateBonus, missileCriticalRateBonus, shipBasedCriticalRateBonus);
    }

    public double getCriticalDamageBonus(String subType) {

        return fetchBonus(subType, ballisticCriticalDamageBonus, 0.0d, 0.0d, shipBasedCriticalDamageBonus);
    }

    public double getDamageDealtBonus(String subType) {

        return fetchBonus(subType, ballisticDamageBonus, laserDamageBonus, missileDamageBonus, shipBasedDamageBonus);
    }

    public void pass(ResearchLevelMeta researchLevelMeta) {

        for (ResearchEffectMeta effectMeta : researchLevelMeta.getEffects()) {
            pass(effectMeta);
        }
    }

    public void pass(ResearchEffectMeta effectMeta) {

        // ? Laser pass
        switch (effectMeta.getType()) {
            case "laser.damage": // Optics & Weakness Detection
                laserDamageBonus += effectMeta.getValue();
                return;
            case "laser.critical.rate": // Directional Malice
                laserCriticalRateBonus += effectMeta.getValue();
                return;
            case "laser.hit.rate": // Directional Accuracy
                laserHitRateBonus += effectMeta.getValue();
                return;
            case "laser.increase.steering.damage": // Eagle Eye
                laserSteeringPowerBonus += effectMeta.getValue();
                return;
            case "laser.penetration": // Energy Penetration
                laserPenetration += effectMeta.getValue();
                return;
            case "laser.increase.dispersion.damage": // Pierce & Improved Pierce
                laserPiercing += effectMeta.getValue();
                return;
            case "laser.increase.dispersion.blast": // Piercing Crit
                laserPiercedCriticalStrike = true;
                return;
            case "laser.decrease.target.damage": // Particle Impact Tech
                laserDecreaseTargetDamage += effectMeta.getValue();
                return;
            case "laser.decrease.target.damage.rounds": // Particle Impact Tech
                laserDecreaseTargetDamageRounds += effectMeta.getValue();
                return;
            case "laser.one.grid.increase.critical.rate": // Energy Accumulation
                laserMovementCriticalRateBonus += effectMeta.getValue();
                return;
            case "laser.decrease.target.ratio": // Radiative Interference
                laserRadiativeInterference += effectMeta.getValue();
                return;
            case "laser.decrease.target.maximum.ratio": // Radiative Interference
                laserRadiativeInterferenceMaximum += effectMeta.getValue();
                return;
            case "laser.decrease.target.steering.reduction.rate": // Electronic Interference
                laserReduceSteeringPowerChance += effectMeta.getValue();
                return;
            case "laser.decrease.target.steering.reduction.damage": // Electronic Interference
                laserReduceSteeringPowerRate += effectMeta.getValue();
                return;
            case "laser.increase.maximum.ratio.chance": // Electronic Interference
                laserIncreaseMaximumHitRateChance += effectMeta.getValue();
                return;
            case "laser.increase.target.yare.ignore.chance": // Weakness Detection
                laserIgnoreAgilityChance += effectMeta.getValue();
                return;
            case "laser.increase.target.yare.ignore.value": // Weakness Detection
                laserIgnoreAgilityRate += effectMeta.getValue();
                return;
            case "laser.increase.target.defense.ignore.value": // Dynamic Impairment
                laserReduceDefenseRateBonus += effectMeta.getValue();
                return;
            case "laser.decrease.target.movement.chance": // Dynamic Impairment
                laserFleetMovementReductionChance += effectMeta.getValue();
                return;
            case "laser.decrease.target.movement.value": // Dynamic Impairment
                laserFleetMovementReductionAmount += effectMeta.getValue();
                return;
            case "laser.increase.range": // Artillery Trajectory Research
                laserIncreaseRangeAmount += effectMeta.getValue();
                return;
        }

        // ? Ballistic pass
        switch (effectMeta.getType()) {
            case "ballistic.damage": // Ballistics
                ballisticDamageBonus += effectMeta.getValue();
                return;
            case "ballistic.critical.rate": // Ballistics Malice
                ballisticCriticalRateBonus += effectMeta.getValue();
                return;
            case "ballistic.critical.damage": // Ballistics Crackdown
                ballisticCriticalDamageBonus += effectMeta.getValue();
                return;
            case "ballistic.hit.rate": // Precise Ballistics
                ballisticHitRateBonus += effectMeta.getValue();
                return;
            case "ballistic.increase.dispersion.damage": // Ballistic Scattering & Improved Ballistic Scattering
                ballisticScattering += effectMeta.getValue();
                return;
            case "ballistic.penetration": // Shield Penetration & Artillery Specialization
                ballisticPenetration += effectMeta.getValue();
                return;
            case "ballistic.kinetic.increase.damage.against.neutralizing": // Depleted Uranium Bomb Research
                ballisticKineticDamageAgainstNeutralizingRate += effectMeta.getValue();
                return;
            case "ballistic.kinetic.increase.damage.against.light": // Depleted Uranium Bomb Research
                ballisticKineticDamageAgainstLightRate += effectMeta.getValue();
                return;
            case "ballistic.kinetic.increase.damage.against.unknown": // Depleted Uranium Bomb Research
                ballisticKineticDamageAgainstUnknownRate += effectMeta.getValue();
                return;
            case "ballistic.heat.increase.damage.against.regen": // Fire Bomb Research
                ballisticHeatDamageAgainstRegenRate += effectMeta.getValue();
                return;
            case "ballistic.heat.increase.damage.against.light": // Fire Bomb Research
                ballisticHeatDamageAgainstLightRate += effectMeta.getValue();
                return;
            case "ballistic.heat.increase.damage.against.unknown": // Fire Bomb Research
                ballisticHeatDamageAgainstUnknownRate += effectMeta.getValue();
                return;
            case "ballistic.increase.damage.against.light": // Improved Penetration
                ballisticDamageAgainstLightRate += effectMeta.getValue();
                return;
            case "ballistic.increase.damage.against.unknown": // Improved Penetration
                ballisticDamageAgainstUnknownRate += effectMeta.getValue();
                return;
            case "ballistic.range.1.increase.damage": // Victory Rush
                ballisticRange1IncreaseDamageRateBonus = effectMeta.getValue();
                return;
            case "ballistic.range.2.increase.damage": // Victory Rush
                ballisticRange2IncreaseDamageRateBonus = effectMeta.getValue();
                return;
            case "ballistic.range.3.increase.damage": // Victory Rush
                ballisticRange3IncreaseDamageRateBonus = effectMeta.getValue();
                return;
            case "ballistic.range.4.increase.damage": // Victory Rush
                ballisticRange4IncreaseDamageRateBonus = effectMeta.getValue();
                return;
            case "ballistic.increase.dispersion.maximum.chance": // Hop Bomb Research
                ballisticIncreaseDispersionMaximumChance += effectMeta.getValue();
                return;
            case "ballistic.increase.dispersion.maximum.damage": // Hop Bomb Research
                ballisticIncreaseDispersionMaximumDamageRateBonus += effectMeta.getValue();
                return;
            case "ballistic.increase.target.damage.on.hphit": // Demolition Warhead Research
                ballisticDamageOnHpHit += effectMeta.getValue();
                return;
            case "ballistic.increase.target.damage.on.hp.or.less": // Demolition Warhead Research
                ballisticDamageOnHpOrLess += effectMeta.getValue();
                return;
            case "ballistic.increase.range": // Range Extension
                ballisticIncreaseRange += effectMeta.getValue();
                return;
            case "ballistic.increase.damage.against.chrome": // Artillery Specialization
                ballisticDamageAgainstChromeRate += effectMeta.getValue();
                return;
            case "ballistic.increase.damage.against.regen": // Artillery Specialization
                ballisticDamageAgainstRegenRate += effectMeta.getValue();
                return;
            case "ballistic.increase.damage.against.nano": // Artillery Specialization
                ballisticDamageAgainstNanoRate += effectMeta.getValue();
                return;
            case "ballistic.increase.damage.against.neutralizing": // Artillery Specialization
                ballisticDamageAgainstNeutralizingRate += effectMeta.getValue();
                return;
        }

        // ? Missile pass
        switch (effectMeta.getType()) {
            case "missile.damage": // Missile Theory & Break Armor
                missileDamageBonus += effectMeta.getValue();
                return;
            case "missile.hit.rate": // Missile Accuracy & Missile Elusion
                missileHitRateBonus += effectMeta.getValue();
                return;
            case "missile.increase.steering.damage": // Cruise Dynamics
                missileSteeringPowerBonus += effectMeta.getValue();
                return;
            case "missile.penetration": // Missile Research & Perfect Storm
                missilePenetration += effectMeta.getValue();
                return;
            case "missile.increase.dispersion.damage": // Multidirectional Assault & Shrapnel Research & Perfect Storm
                missileScattering += effectMeta.getValue();
                return;
            case "missile.decrease.intercept.rate": // Missile Elusion & Perfect Storm
                missileInterceptRateBonus += effectMeta.getValue();
                return;
            case "missile.increase.lower.structure.dispersion.damage": // Exaltation
                missileLowerStructureScattering += effectMeta.getValue();
                return;
            case "missile.increase.he3.reduction.rate": // Exaltation
                missileHE3ReductionRateBonus += effectMeta.getValue();
                return;
            case "missile.critical.rate": // Suppression
                missileCriticalRateBonus += effectMeta.getValue();
                return;
            case "missile.increase.higher.structure.dispersion.damage": // Suppression
                missileHigherStructureScattering += effectMeta.getValue();
                return;
            case "missile.increase.target.fleet.damage.chance": // Nuclear Radiation Research
                missileAdditionalIncreaseDamageChance += effectMeta.getValue();
                return;
            case "missile.increase.target.fleet.damage.value": // Nuclear Radiation Research
                missileAdditionalIncreaseDamageValue += effectMeta.getValue();
                return;
            case "missile.increase.he3.reduction.chance": // Energy Conservation & Perfect Storm
                missileAdditionalHE3ReductionChance += effectMeta.getValue();
                return;
            case "missile.increase.he3.reduction.value": // Energy Conservation
                missileAdditionalHE3ReductionValue += effectMeta.getValue();
                return;
            case "missile.increase.repel.chance": // Missile Concussion & Rapid Loading & Perfect Storm
                missileRepelChance += effectMeta.getValue();
                return;
            case "missile.increase.repel.range": // Missile Concussion & Rapid Loading
                missileRepelRange += effectMeta.getValue();
                return;
            case "missile.decrease.weapon.cooldown.round": // Rapid Loading
                missileCooldownReduction += effectMeta.getValue();
                return;
            case "missile.increase.range": // Perfect Storm
                missileIncreaseRange += effectMeta.getValue();
                return;
        }

        // ? Ship-based pass
        switch (effectMeta.getType()) {
            case "shipbased.damage": // Fighter Weapons Theory & Fighter Mastery & Fighter Tech Upgrades & Armor Structural Analysis
                shipBasedDamageBonus += effectMeta.getValue();
                return;
            case "shipbased.hit.rate": // Navigation
                shipBasedHitRateBonus += effectMeta.getValue();
                return;
            case "shipbased.decrease.intercept.rate": // Thruster Optimization & Fighter Interception Countermeasures & Heavy Gear Research
                shipBasedInterceptRateBonus += effectMeta.getValue();
                return;
            case "shipbased.increase.steering.damage": // Reconnaissance
                shipBasedSteeringPowerBonus += effectMeta.getValue();
                return;
            case "shipbased.increase.he3.reduction.rate": // Fuel Optimization & Fighter Tech Upgrades & Ingenuity
                shipBasedHE3ReductionRateBonus += effectMeta.getValue();
                return;
            case "shipbased.long.ranged.away.6.slots.increase.base.damage": // Long-ranged Strike
                shipBasedLongRangedAway6SlotsIncreaseBaseDamage += effectMeta.getValue();
                return;
            case "shipbased.long.ranged.away.7.slots.increase.base.damage": // Long-ranged Strike
                shipBasedLongRangedAway7SlotsIncreaseBaseDamage += effectMeta.getValue();
                return;
            case "shipbased.long.ranged.away.8.slots.increase.base.damage": // Long-ranged Strike
                shipBasedLongRangedAway8SlotsIncreaseBaseDamage += effectMeta.getValue();
                return;
            case "shipbased.long.ranged.away.9.slots.increase.base.damage": // Long-ranged Strike
                shipBasedLongRangedAway9SlotsIncreaseBaseDamage += effectMeta.getValue();
                return;
            case "shipbased.long.ranged.away.10.slots.increase.base.damage": // Long-ranged Strike
                shipBasedLongRangedAway10SlotsIncreaseBaseDamage += effectMeta.getValue();
                return;
            case "shipbased.critical.damage": // Formation Optimization
                shipBasedCriticalDamageBonus += effectMeta.getValue();
                return;
            case "shipbased.improve.hit.chance": // Swarm & Ingenuity
                shipBasedImproveHitChance += effectMeta.getValue();
                return;
            case "shipbased.improve.hit.damage": // Swarm & Ingenuity
                shipBasedImproveHitDamageBonus += effectMeta.getValue();
                return;
            case "shipbased.improve.hit.he3.cost": // Swarm
                shipBasedImproveHitHe3Cost += effectMeta.getValue();
                return;
            case "shipbased.instant.reload.chance": // Fighter-based Weapons Efficiency
                shipBasedInstantReloadChance += effectMeta.getValue();
                return;
            case "shipbased.critical.rate": // Fortune
                shipBasedCriticalRateBonus += effectMeta.getValue();
                return;
            case "shipbased.long.ranged.away.increase.double.chance": // Fortune
                shipBasedLongRangedAwayDoubleChance += effectMeta.getValue();
                return;
            case "shipbased.custom.intercept.rate": // Ingenuity
                shipBasedCustomInterceptRate += effectMeta.getValue();
                return;
            case "shipbased.increase.range": // Heavy Gear Research
                shipBasedIncreaseRange += effectMeta.getValue();
                return;
            case "shipbased.decrease.weapon.cooldown.round": // Heavy Gear Research
                shipBasedCooldownReduction += effectMeta.getValue();
                return;
            case "shipbased.enemy.no.shield.increase.base.damage": // Ingenuity
                shipBasedEnemyNoShieldIncreaseDamageBonus += effectMeta.getValue();
                return;
            case "shipbased.enemy.shield.increase.damage": // Heavy Gear Research
                shipBasedEnemyWithShieldIncreaseDamageBonus += effectMeta.getValue();
                return;
        }

        // ? Defense pass
        switch (effectMeta.getType()) {
            case "increase.ship.shield": // Ship Defense Tech & Shield Research & Augment Shield
                shipShieldBonus += effectMeta.getValue();
                return;
            case "increase.ship.endure": // Ship Defense Tech & Ship Structural Analysis & Structure Improvement
                shipArmorBonus += effectMeta.getValue();
                return;
            case "increase.ship.yare": // Ship Defense Tech & Resilience
                shipAgilityBonus += effectMeta.getValue();
                return;
            case "increase.ship.defend": // Ship Defense Tech & Defense Improvement
                shipDaedalusBonus += effectMeta.getValue();
                return;
            case "increase.ship.stability": // Ship Defense Tech & Ship Reinforcement Tech & Reaction Armor Improvement
                shipStabilityBonus += effectMeta.getValue();
                return;
            case "shield.module.increase.damage.reduction": // Energy Diffusion Tech
                shieldModuleDamageReduction += effectMeta.getValue();
                return;
            case "enemy.critical.strike.increase.damage.reduction": // Resilience & Augment Absorption
                enemyCriticalStrikeDamageReduction += effectMeta.getValue();
                return;
            case "enemy.shield.penetration.reduction.rate": // Penetration Resistance
                enemyShieldPenetrationReductionRateBonus += effectMeta.getValue();
                return;
            case "reaction.armor.harm.increase.reduction": // Reaction Armor Improvement
                reactionArmorDamageReductionBonus += effectMeta.getValue();
                return;
            case "armor.type.chrome.increase.reduction": // Defense Improvement
                armorTypeChromeDamageReductionBonus += effectMeta.getValue();
                return;
            case "armor.type.nano.increase.reduction": // Defense Improvement
                armorTypeNanoDamageReductionBonus += effectMeta.getValue();
                return;
            case "armor.type.neutralizing.increase.reduction": // Defense Improvement
                armorTypeNeutralizingDamageReductionBonus += effectMeta.getValue();
                return;
            case "armor.type.regen.increase.reduction": // Defense Improvement
                armorTypeRegenDamageReductionBonus += effectMeta.getValue();
                return;
            case "armor.type.light.increase.reduction": // Defense Improvement
                armorTypeLightDamageReductionBonus += effectMeta.getValue();
                return;
            case "armor.type.unknown.increase.reduction": // Defense Improvement (! todo: check this)
                armorTypeUnknownDamageReductionBonus += effectMeta.getValue();
                return;
            case "all.modules.no.he3.consumption.increase.rate": // Energy Conservation
                allNegationModulesNoHe3ConsumptionRateBonus += effectMeta.getValue();
                return;
            case "enable.reflect.modules.reactivation.after.rounds": // Reflection Mastery
                enableReflectModulesReactivationAfterRounds = effectMeta.getValue();
                return;
            case "after.shield.damage.reflection": // Electronic Barrier
                beforeShieldDamageReflectionRateBonus = effectMeta.getValue();
                return;
            case "shield.module.increase.restoration.at.round.start": // Restoration
                shieldHealRateBonus += effectMeta.getValue();
                return;
            case "intercept.module.increase.rate": // Restoration & Fast Repair Tech
                interceptModuleRateBonus += effectMeta.getValue();
                return;
            case "structure.module.restoration.at.round.start": // Fast Repair Tech
                armorHealRateBonus += effectMeta.getValue();
                return;
            case "module.double.damage.absorption.chance": // Damage Mitigation
                moduleDoubleDamageAbsorptionChanceBonus += effectMeta.getValue();
                return;
            case "scattering.increase.damage.reduction": // Damage Mitigation & Stability Mastery
                scatteringDamageReductionRateBonus += effectMeta.getValue();
                return;
            case "activated.daedalus.ship.stability.to.augment.during.attack.increase.rate": // Stability Mastery
                activatedDaedalusShipStabilityAugmentChance += effectMeta.getValue();
                return;
            case "activated.daedalus.ship.stability.augment": // Stability Mastery
                activatedDaedalusShipStabilityAugment += effectMeta.getValue();
                return;
            case "activated.daedalus.ship.increase.damage.reduction": // Stability Mastery
                activatedDaedalusShipDamageReduction += effectMeta.getValue();
        }

    }

    public double fetchBonus(String subType, double ballisticBonus, double directionalBonus, double missileBonus, double shipBasedBonus) {

        switch (subType) {

            case "ballistic":
                return ballisticBonus;
            case "directional":
                return directionalBonus;
            case "missile":
                return missileBonus;
            case "shipBased":
                return shipBasedBonus;

        }

        return 0.0d;

    }

}

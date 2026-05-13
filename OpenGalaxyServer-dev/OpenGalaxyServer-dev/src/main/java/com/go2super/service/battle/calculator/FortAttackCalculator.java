package com.go2super.service.battle.calculator;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleFort;
import com.go2super.database.entity.sub.battle.meta.FortCellAttackMeta;
import com.go2super.database.entity.sub.battle.trigger.FortAttackFleetTrigger;
import com.go2super.database.entity.type.SpaceFortType;
import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import com.go2super.socket.util.MathUtil;

import java.util.*;
import java.util.stream.*;

public class FortAttackCalculator {

    private final MatchRunnable matchRunnable;

    private final BattleFort attackerFort;
    private final BattleFleet defenderFleet;

    public FortAttackCalculator(MatchRunnable matchRunnable, BattleFort attacker, BattleFleet defender) {

        this.matchRunnable = matchRunnable;
        this.attackerFort = attacker;
        this.defenderFleet = defender;

    }

    public FortAttackCalculator calculate(int uniqueDamage, List<ShipPosition> positions, FortAttackFleetTrigger trigger) {

        List<BattleFleetCell> targetCells = defenderFleet.getCells().stream().filter(cell -> cell.hasShips()).collect(Collectors.toList());
        if (targetCells.isEmpty()) {
            return this;
        }

        for (ShipPosition position : positions) {

            FortCellAttackMeta attackMeta = new FortCellAttackMeta();

            attackMeta.setShipReductions(new ArrayList<>());
            attackMeta.setFortShootdowns(new ArrayList<>());

            attackMeta.setDefenderPos(position.getPos());
            attackMeta.setDefenderSegmentedPosIndex(position.getSegmentedPosIndex());
            attackMeta.setDefenderPosIndex(position.getPosIndex());
            attackMeta.setDefenderDirection(position.getDirection());

            calculate(uniqueDamage, position, attackMeta, trigger);

        }
        return this;

    }

    private FortAttackCalculator calculate(int uniqueDamage, ShipPosition position, FortCellAttackMeta attackMeta, FortAttackFleetTrigger trigger) {

        BattleFleetCell defenderCell = position.getBattleFleetCell();
        List<BattleFleetDefensiveModule> defensiveModules = defenderCell.getDefensiveModules();

        if (!defenderCell.hasShips()) {
            return this;
        }

        // Divide defensive modules by subTypes
        BattleCommander defenderCommander = defenderFleet.getBattleCommander();
        int defenderEffectiveStack = defenderCommander.getEffectiveStack(defenderCell);

        ShipUsage defenderUsage = new ShipUsage();
        ShipTechs defenderTechs = defenderFleet.getTechs();

        //
        // ? Based on: https://galaxyonlineii.fandom.com/wiki/Combat_Mechanics#hit_chance_formula
        // ? Next we will follow all of these steps for make the combat calculation
        //

        double defenderArmor = defenderCell.getStructure();
        double defenderShield = defenderCell.getShields();

        // * [Step -1. Calculate additional effects] - Passive modules
        // * body type passives.
        ShipStats defenderStats = new ShipStats();

        // ? Ship models effects aggregation
        defenderStats.pass(defenderFleet);
        defenderStats.pass(defenderCell.getModel());

        // * [Step 0. Total damage mitigation] (All types) - Calculate all damage
        // * that can be negated by the shields and structure and save in a
        // * data structure for be used to know how many modules should use.
        ShipDefense shipDefense = new ShipDefense();

        LinkedHashMap<Integer, List<BattleFleetDefensiveModule>> mappedMitigations = defensiveModules.stream()
            .collect(Collectors.groupingBy(module -> module.getModuleId(), LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<BattleFleetDefensiveModule>> mitigateModule : mappedMitigations.entrySet()) {

            ShipMitigation shipMitigation = new ShipMitigation();

            for (BattleFleetDefensiveModule defensiveModule : mitigateModule.getValue()) {

                double baseNegation = defensiveModule.getDamageNegate();
                double kindNegation = defensiveModule.getNegation();

                shipMitigation.add(defensiveModule, baseNegation, new ArrayList<>(), kindNegation);

            }

            if (shipMitigation.getReference().isShield()) {

                shipDefense.getShieldMitigations().add(shipMitigation);
                continue;

            }

            shipDefense.getArmorMitigations().add(shipMitigation);

        }

        // * [Step 0.3. List necessary attack modules] - List all attack modules
        // * needed for destroy the whole defenderStack (this will take into account
        // * negation and interception).
        double virtualDamage = 0;

        double virtualShield = defenderShield;
        double virtualArmor = defenderArmor;

        double trueShieldDamage = 0;
        double trueArmorDamage = 0;

        double defenderHe3 = defenderFleet.getHe3();

        Iterator<ShipMitigation> shieldIterator = shipDefense.getShieldMitigations().listIterator();
        Iterator<ShipMitigation> armorIterator = shipDefense.getArmorMitigations().listIterator();

        // ? Set attack basics
        attackMeta.setAttack(true);

        // ? Additional information
        double additionalAbsorption = defenderCommander.getAdditionalAbsorption();

        // ? Calculate damages
        double calculatedDamage = uniqueDamage;
        virtualDamage = calculatedDamage;

        // ? Apply daedalus reduction (Formula
        // ? extracted from krtools)
        double daedalus = defenderStats.getDaedalus();
        virtualDamage -= calculatedDamage * (daedalus * 0.03 / (daedalus * 0.03 + 1));

        double virtualShieldMitigation = 0.0d;

        // ? Calculate shield damage mitigation
        while (virtualShield > 0 && virtualDamage > 0 && shieldIterator.hasNext()) {

            ShipMitigation mitigation = shieldIterator.next();

            double fuelUsage = mitigation.getFuelUsage(1);
            fuelUsage = Math.max(fuelUsage - (fuelUsage * defenderTechs.getAllNegationModulesNoHe3ConsumptionRateBonus()), 0);
            if (defenderHe3 < (fuelUsage * defenderEffectiveStack)) {
                continue;
            }

            mitigation.addUsage(defenderUsage);
            defenderHe3 = Math.max(defenderHe3 - (fuelUsage * defenderEffectiveStack), 0);

            double mitigationIncrement = defenderTechs.getShieldModuleDamageReduction();
            double oneMitigation = (mitigation.getTotalMitigation("explosive") + mitigationIncrement) * 1;
            double doubleMitigationChance = Math.min(defenderTechs.getModuleDoubleDamageAbsorptionChanceBonus(), 1.0d);
            if (MathUtil.randomInclusive(1, 100) <= (doubleMitigationChance * 100.0d)) {
                oneMitigation *= 2;
            }

            double shieldMitigation = oneMitigation * defenderEffectiveStack;
            shieldMitigation = Math.max((shieldMitigation + additionalAbsorption), 0);
            virtualShieldMitigation += Math.max(shieldMitigation, 0);

            if (Math.max(virtualShieldMitigation - virtualDamage, 0) == 0) {

                virtualDamage = Math.max(virtualDamage - virtualShieldMitigation, 0);
                virtualShieldMitigation = 0;
                continue;

            }

            // check h3 overconsumption
            double checkMitigation = virtualShieldMitigation;

            while (checkMitigation > virtualDamage) {
                checkMitigation = Math.max(checkMitigation - oneMitigation, 0);
                defenderHe3 += fuelUsage;
            }

            defenderHe3 = Math.max(defenderHe3 - fuelUsage, 0);

            // Apply final mitigation
            virtualShieldMitigation = virtualShieldMitigation - virtualDamage;
            virtualDamage = 0;

        }

        // ? Apply shield damage with reductions
        if (virtualDamage >= virtualShield && trueShieldDamage < virtualShield) {
            trueShieldDamage += virtualShield;
            virtualDamage -= virtualShield;
        } else if (trueShieldDamage < virtualShield) {
            trueShieldDamage += virtualDamage;
            virtualDamage = 0;
        }

        double virtualArmorMitigation = 0.0d;

        // ? Calculate armor damage mitigation
        while (virtualArmor > 0 && virtualDamage > 0 && armorIterator.hasNext()) {

            ShipMitigation mitigation = armorIterator.next();

            double fuelUsage = mitigation.getFuelUsage(1);
            fuelUsage = Math.max(fuelUsage - (fuelUsage * defenderTechs.getAllNegationModulesNoHe3ConsumptionRateBonus()), 0);
            if (defenderHe3 < (fuelUsage * defenderEffectiveStack)) {
                continue;
            }

            mitigation.addUsage(defenderUsage);
            defenderHe3 = Math.max(defenderHe3 - (fuelUsage * defenderEffectiveStack), 0);

            double oneMitigation = mitigation.getTotalMitigation("explosive") * 1;
            double doubleMitigationChance = Math.min(defenderTechs.getModuleDoubleDamageAbsorptionChanceBonus(), 1.0d);
            if (MathUtil.randomInclusive(1, 100) <= (doubleMitigationChance * 100.0d)) {
                oneMitigation *= 2;
            }

            double armorMitigation = oneMitigation * defenderEffectiveStack;
            armorMitigation = Math.max((armorMitigation + additionalAbsorption) * 1, 0);
            virtualArmorMitigation += Math.max(armorMitigation, 0);

            if (Math.max(virtualArmorMitigation - virtualDamage, 0) == 0) {

                virtualDamage = Math.max(virtualDamage - virtualArmorMitigation, 0);
                virtualArmorMitigation = 0;
                continue;

            }

            // check h3 overconsumption
            double checkMitigation = virtualShieldMitigation;

            while (checkMitigation > virtualDamage) {
                checkMitigation = Math.max(checkMitigation - oneMitigation, 0);
                defenderHe3 += fuelUsage;
            }

            defenderHe3 = Math.max(defenderHe3 - fuelUsage, 0);

            // Apply final mitigation
            virtualArmorMitigation = virtualArmorMitigation - virtualDamage;
            virtualDamage = 0;

        }

        // ? Apply armor damage with reductions
        if (virtualDamage >= virtualArmor && trueArmorDamage < virtualArmor) {
            trueArmorDamage += virtualArmor;
            virtualDamage -= virtualArmor;
        } else if (trueArmorDamage < virtualArmor) {
            trueArmorDamage += virtualDamage;
            virtualDamage = 0;
        }

        if (!attackMeta.isAttack()) {
            return this;
        }

        // ? Fleet HE3 adjustments before
        // ? the reductions
        if (defenderHe3 > defenderFleet.getHe3()) {
            defenderHe3 = defenderFleet.getHe3();
        }

        // * [Step 10. Calculate Supplies] - Based on the current usages calculate
        // * the supply reduction.
        // * todo
        double trueDefenderSupply = defenderFleet.getHe3() - defenderHe3;

        attackMeta.setFromFortId(attackerFort.getFortId());
        attackMeta.setFromUserGuid(attackerFort.getGuid());

        attackMeta.setToShipTeamId(defenderFleet.getShipTeamId());
        attackMeta.setToUserGuid(defenderFleet.getGuid());

        attackMeta.setToAmount(defenderCell.getAmount());

        trigger.getAttacks().add(attackMeta);

        ShipReduction shipReduction = defenderCell.makeReduction(trueShieldDamage, trueArmorDamage, trueDefenderSupply, false, defenderStats, defenderFleet);
        shipReduction.setPosition(position);
        attackMeta.getShipReductions().add(shipReduction);

        attackMeta.targetShipTeamId = defenderFleet.getShipTeamId();
        attackMeta.targetReduceSupply += (int) shipReduction.getSupplyReduction();
        attackMeta.targetReduceStorage += (int) shipReduction.getStorageReduction();

        int hp = (int) shipReduction.getShieldsReduction() + (int) shipReduction.getStructureReduction();

        attackMeta.targetReduceHp[attackMeta.getDefenderPos()] = hp;
        attackMeta.targetReduceShipNum[attackMeta.getDefenderPos()] = (int) shipReduction.getAmountReduction();

        if (shipReduction.getAmountReduction() > 0) {

            FortShootdowns shootdown = new FortShootdowns();

            shootdown.setAttacker(defenderFleet.isAttacker());
            shootdown.setAmount((int) shipReduction.getAmountReduction());

            attackMeta.getFortShootdowns().add(shootdown);

        }

        return this;

    }

}

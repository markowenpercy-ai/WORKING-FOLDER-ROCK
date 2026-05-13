package com.go2super.service.battle.calculator;

import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BattleElement;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleFort;
import com.go2super.database.entity.sub.battle.meta.AssaultCellAttackMeta;
import com.go2super.database.entity.sub.battle.meta.ShipCellAttackMeta;
import com.go2super.database.entity.sub.battle.trigger.FleetAttackFleetTrigger;
import com.go2super.database.entity.sub.battle.trigger.FleetAttackFortTrigger;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.service.CommanderService;
import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.service.battle.module.BattleFleetAuxiliaryModule;
import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import com.go2super.service.battle.module.ShipModule;
import com.go2super.service.battle.type.EffectType;
import com.go2super.service.battle.type.ScatteringType;
import com.go2super.socket.util.MathUtil;
import com.go2super.socket.util.RandomUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ShipAttackCalculator {

    private final MatchRunnable matchRunnable;

    private final BattleFleet attackerFleet;
    private final BattleElement defenderElement;

    private final int defensiveDirection;

    private final int steps;
    private final int round;

    private boolean attackerCommanderAnnahilation = false;
    private boolean attackerCommanderRepelSteps = false;
    private boolean defenderFallbackTech;

    private boolean fullCritical;
    private boolean fullDouble;

    // Sorted by defensive direction, for no sorted just defenderFleet.getTeam().getCells()
    public ShipAttackCalculator(MatchRunnable matchRunnable, BattleFleet attacker, BattleElement defender, int defensiveDirection, int steps) {

        this.matchRunnable = matchRunnable;
        this.attackerFleet = attacker;
        this.defenderElement = defender;

        this.defensiveDirection = defensiveDirection;

        this.steps = steps;
        this.round = matchRunnable.getMatch().getRound();

    }

    public ShipAttackCalculator calculate(ShipPosition attacker, ShipPosition target, ShipCellAttackMeta attackMeta, FleetAttackFleetTrigger trigger, List<ShipPosition> sortedDefenderPositions) {

        if (!(defenderElement instanceof BattleFleet defenderFleet)) {
            return this;
        }

        BattleFleetCell defenderCell = target.getBattleFleetCell();
        BattleFleetCell attackerCell = attacker.getBattleFleetCell();

        ShipEffects defenderCellEffects = defenderCell.getEffects();
        ShipEffects attackerCellEffects = attackerCell.getEffects();

        // Skill's Global Variables
        boolean defenderNullified = defenderFleet.isNullified();
        boolean attackerNullified = attackerFleet.isNullified();

        BattleCommander attackCommander = attackerFleet.getBattleCommander();
        BattleCommander defenderCommander = defenderFleet.getBattleCommander();

        // ? Get commanders gems/chips/base and other bonuses like skills
        double commanderAttackPowerBonus = attackCommander.getAttackPowerIncrement();
        double commanderCriticalAttackDamageBonus = attackCommander.getCriticalAttackDamageIncrement();
        double commanderCriticalAttackRateBonus = attackCommander.getCriticalAttackRateIncrement();
        double commanderDoubleAttackRateBonus = attackCommander.getDoubleAttackRateIncrement();

        double commanderReflectDamageBonus = 0.0d;
        double commanderInterceptionRateBonus = 0.0d;
        double commanderStabilityRateBonus = 0.0d;
        double commanderAccuracyBonus = 0.0d;

        double commanderVerticalScatteringBonus = 0.0d;
        double commanderHorizontalScatteringBonus = 0.0d;
        double commanderOverallScatteringBonus = 0.0d;

        double commanderDamageReductionBonus = 0.0d;
        double commanderShieldNegationBonus = 0.0d;
        double commanderReflectBonus = 0.0d;

        double commanderStructureDamageBasedShieldBonus = 0.0d;
        double commanderPierceBonus = 0.0d;

        double additionalNetDamage = 0.0d;
        double additionalAttackerHe3Cost = 0.0d;
        double additionalDefenderHe3Cost = 0.0d;

        boolean commanderIgnoreDaedalus = false;
        boolean commanderIgnoreAgility = false;
        boolean commanderIgnoreDefensiveModulesEnabled = false;
        boolean commanderIgnoreDamageEnabled = false;
        boolean commanderIgnoreScatteringEnabled = false;
        boolean commanderFormationPenaltyDisabled = false;
        boolean commanderHighestHitRateEnabled = false;

        boolean commanderInstantaneousReload = false;
        boolean commanderMaximumStability = false;
        boolean commanderEquitationDamage = false;
        boolean commanderReflectEverything = false;
        boolean commanderTriggerBlast = false;
        boolean commanderBreakthroughEnabled = false;
        boolean attackerSilent = false;
        boolean defenderSilent = false;
        boolean attackerIgnoreReflect = false;

        // ! Dopplegangers [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:dopplegangers")) {
            attackerFleet.setOtherSkill(defenderFleet.getBattleCommander().getNameId());
            attackCommander.setTrigger(CommanderService.getInstance().createTrigger(attackCommander, defenderCommander));
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:dopplegangers")) {
            defenderFleet.setOtherSkill(attackerFleet.getBattleCommander().getNameId());
            defenderCommander.setTrigger(CommanderService.getInstance().createTrigger(defenderCommander, attackCommander));
        }

        // ! Singhri [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:singhri")) {
            attackerFleet.setOtherSkill(defenderFleet.getBattleCommander().getNameId());
            attackCommander.setTrigger(CommanderService.getInstance().createTrigger(attackCommander, defenderCommander));
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:singhri")) {
            defenderFleet.setOtherSkill(attackerFleet.getBattleCommander().getNameId());
            defenderCommander.setTrigger(CommanderService.getInstance().createTrigger(defenderCommander, attackCommander));
        }

        // ! Rex Scuta [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:rexScuta")) {
            if (attackCommander.isTotalMoreThan(defenderCommander)) {
                defenderSilent = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Rex Scuta [Skill] #2
        if (!defenderSilent && defenderFleet.isCommanded("commander:rexScuta")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                commanderMaximumStability = true;
                attackerSilent = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Death from Above [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:deathFromAbove")) {
            if (attackCommander.isTotalMoreThan(defenderCommander)) {
                defenderSilent = true;
                commanderTriggerBlast = true;
                attackMeta.sourceSkill = 1;
            }
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:deathFromAbove")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                attackerSilent = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! The Dictators [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:dictators")) {
            if (attackCommander.isTotalMoreThan(defenderCommander)) {
                defenderSilent = true;
                attackMeta.sourceSkill = 1;
            }
        }

        if (defenderFleet.isCommanded("commander:dictators")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                attackerSilent = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Rayo [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:rayo")) {
            if (attackCommander.isTotalMoreThan(defenderCommander)) {
                defenderSilent = true;
                attackMeta.sourceSkill = 1;
            }
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:rayo")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                attackerSilent = true;
                attackMeta.targetSkill = 1;
            }
        }


        if (!attackerSilent && attackerFleet.isCommanded("commander:javal")) {
            if (attackerFleet.trigger("commander:javal")) {
                defenderSilent = true;
                attackMeta.sourceSkill = 1;
            }
        }

        if (!attackerSilent && attackerFleet.isCommanded("commander:lumos")) {
            if (attackerFleet.trigger("commander:lumos")) {
                attackerIgnoreReflect = true;
                attackMeta.sourceSkill = 1;
            }
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:javal")) {
            if (defenderFleet.trigger("commander:javal")) {
                attackerSilent = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Technomancers [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:technomancers")) {
            if (attackerFleet.trigger("commander:technomancers")) {
                trigger.addEffect(defenderFleet, EffectType.REGGIE, 1, matchRunnable.getMatch().getRound() + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:technomancers")) {
            if (defenderFleet.trigger("commander:technomancers")) {
                trigger.addEffect(attackerFleet, EffectType.CIRCE, 1, matchRunnable.getMatch().getRound() + 1, false);
                attackMeta.targetSkill = 1;
            }
        }

        // ! Reggie [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:reggie")) {
            if (attackerFleet.trigger("commander:reggie")) {
                trigger.addEffect(defenderFleet, EffectType.REGGIE, 1, matchRunnable.getMatch().getRound() + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Circe [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:circe")) {
            if (defenderFleet.trigger("commander:circe")) {
                trigger.addEffect(attackerFleet, EffectType.CIRCE, 1, matchRunnable.getMatch().getRound() + 1, false);
                attackMeta.targetSkill = 1;
            }
        }

        Node attackerNode = attackerFleet.getNode();
        Node defenderNode = defenderFleet.getNode();

        int distance = attackerNode.getHeuristic(defenderNode);

        // Whole attack modules
        ShipAttack shipAttack = attackerCell.getWeaponsToAttack(matchRunnable.getMatch().getRound() + 1, distance, attackerFleet.getTechs(), false);
        if (shipAttack.getAttackModules().isEmpty()) {
            return this;
        }

        List<BattleFleetAttackModule> attackModules = shipAttack.getAttackModules();
        List<BattleFleetDefensiveModule> defenderDefensiveModules = target.getBattleFleetCell().getDefensiveModules();

        // Divide defensive modules by subTypes
        List<BattleFleetDefensiveModule> interceptModules = defenderDefensiveModules.stream().filter(module -> module.getModuleSubType().equals("intercept") || module.getModuleSubType().equals("shipBased")).toList();

        int attackerEffectiveStack = attackCommander.getEffectiveStack(attackerCell);
        int defenderEffectiveStack = defenderCommander.getEffectiveStack(defenderCell);

        ShipUsage attackerUsage = new ShipUsage();
        ShipUsage defenderUsage = new ShipUsage();

        ShipTechs attackerTechs = attackerFleet.getTechs();
        ShipTechs defenderTechs = defenderFleet.getTechs();

        // ? Get attacker commander electron
        double attackerElectronBonus = (1 + (attackCommander.getTotalElectron() / 12)) * 0.01;

        // ! Eternal Terrors [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:eternalTerrors")) {
            if (attackerFleet.trigger("commander:eternalTerrors")) {

                commanderInstantaneousReload = true;
                trigger.addEffect(defenderFleet, EffectType.REGGIE, 1, matchRunnable.getMatch().getRound() + 1, false);

                attackMeta.sourceSkill = 1;

            }
        }

        // ! Kismet Beams [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:kismetBeams")) {
            if (attackerFleet.trigger("commander:kismetBeams")) {

                commanderAttackPowerBonus += 2.0;
                commanderOverallScatteringBonus += 0.3d;

                attackMeta.sourceSkill = 1;

            }
        }

        // ! Wildfire [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:wildfire")) {
            if (attackerFleet.trigger("commander:wildfire")) {
                commanderOverallScatteringBonus += 0.3d;
                commanderInstantaneousReload = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Sandora [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:sandora")) {
            if (attackerFleet.trigger("commander:sandora")) {
                commanderOverallScatteringBonus += 0.3d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Pernicious Princes [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:perniciousPrinces")) {
            if (attackCommander.isTotalMoreThan(defenderCommander)) {
                commanderTriggerBlast = true;
                commanderIgnoreDaedalus = true;
                commanderIgnoreAgility = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Robert [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:robert")) {
            if (attackCommander.isTotalMoreThan(defenderCommander)) {
                commanderTriggerBlast = true;
            }
        }


        // ! Queens of Blades [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:queensOfBlades")) {
            if (attackerFleet.trigger("commander:queensOfBlades")) {
                trigger.addEffect(attackerFleet, EffectType.CALLISTO, 0.1d, -1);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Callisto [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:callisto")) {
            if (attackerFleet.trigger("commander:callisto")) {
                trigger.addEffect(attackerFleet, EffectType.CALLISTO, 0.1d, -1);
                attackMeta.sourceSkill = 1;
            }
        }

        if (attackerFleet.getEffects().contains(EffectType.CALLISTO)) {
            FleetEffect fleetEffect = attackerFleet.getEffects().getEffect(EffectType.CALLISTO);
            commanderAttackPowerBonus += fleetEffect.getValue();
        }

        // ! Carlos [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:carlos")) {
            if (attackerFleet.trigger("commander:carlos")) {
                commanderStructureDamageBasedShieldBonus = defenderCommander.getTrigger().getRate() * 0.01;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Venus [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:venus")) {
            //100% trigger
            commanderShieldNegationBonus = defenderCommander.getTrigger().getRate() * 0.01;
            attackMeta.targetSkill = 1;
        }

        // ! Desolate Prayers [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:desolatePrayers")) {
            if (attackerFleet.trigger("commander:desolatePrayers")) {
                trigger.addEffect(defenderFleet, EffectType.STANI, 1, round + 1, false);
                trigger.addEffect(defenderFleet, EffectType.AILEEN, 1, round + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Stani [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:stani")) {
            if (attackerFleet.trigger("commander:stani")) {
                trigger.addEffect(defenderFleet, EffectType.STANI, 1, round + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! The Forsaken [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:theForsaken")) {
            if (defenderFleet.trigger("commander:theForsaken")) {
                trigger.addEffect(attackerFleet, EffectType.MEDUSA, 1, round + 1, false);
                attackMeta.targetSkill = 1;
            }
        }

        if (!attackerSilent && attackerFleet.isCommanded("commander:theForsaken")) {
            if (attackerFleet.trigger("commander:theForsaken")) {
                trigger.addEffect(defenderFleet, EffectType.STANI, 1, round + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Medusa [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:medusa")) {
            if (defenderFleet.trigger("commander:medusa")) {
                trigger.addEffect(attackerFleet, EffectType.MEDUSA, 1, round + 1, false);
                attackMeta.targetSkill = 1;
            }
        }

        // ! Bain [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:bain")) {
            if (defenderFleet.trigger("commander:bain")) {
                trigger.addEffect(attackerFleet, EffectType.BAIN, 1, round + 1, false);
                attackMeta.targetSkill = 1;
            }
        }

        // ! Eschaton Adventists [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:eschatonAdventists")) {
            if (attackerFleet.trigger("commander:eschatonAdventists")) {
                trigger.addEffect(defenderFleet, EffectType.AILEEN, 1, round + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! The Heartless Ones [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:theHeartlessOnes")) {
            if (attackerFleet.trigger("commander:theHeartlessOnes")) {
                trigger.addEffect(defenderFleet, EffectType.AILEEN, 1, round + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! The Heartless Ones [Skill] #2
        if (!defenderSilent && defenderFleet.isCommanded("commander:theHeartlessOnes")) {
            if (defenderFleet.trigger("commander:theHeartlessOnes")) {
                trigger.addEffect(attackerFleet, EffectType.BAIN, 1, round + 1, false);
                attackMeta.targetSkill = 1;
            }
        }

        // ! Aileen [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:aileen")) {
            if (attackerFleet.trigger("commander:aileen")) {
                trigger.addEffect(defenderFleet, EffectType.AILEEN, 1, round + 1, false);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Summa Cum Laude [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:summaCumLaude")) {
            if (attackerFleet.trigger("commander:summaCumLaude")) {
                commanderBreakthroughEnabled = true;
                commanderInstantaneousReload = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Marcus [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:marcus")) {
            if (attackerFleet.trigger("commander:marcus")) {
                commanderBreakthroughEnabled = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Bart [Skill]
        if (attackerFleet.isCommanded("commander:bart")) {
            if (attackerFleet.getEffects().contains(EffectType.BART)) {
                attackerFleet.setAnimation(true);
                commanderAttackPowerBonus += 0.15d;
            }
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:bart")) {
            if (defenderFleet.getEffects().contains(EffectType.BART)) {
                defenderFleet.setAnimation(true);
            }
        }

        // ! Rays of Destiny [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:raysOfDestiny")) {
            if (attackerFleet.trigger("commander:raysOfDestiny")) {
                commanderOverallScatteringBonus += 0.3d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Rays of Destiny [Skill] #2
        if (!defenderSilent && defenderFleet.isCommanded("commander:raysOfDestiny")) {
            if (defenderFleet.trigger("commander:raysOfDestiny")) {
                commanderReflectEverything = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Dune Enforcers [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:duneEnforcers")) {
            if (defenderFleet.trigger("commander:duneEnforcers")) {
                commanderReflectEverything = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Maletiz [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:maletiz")) {
            if (defenderFleet.trigger("commander:maletiz")) {
                commanderReflectEverything = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Suicidal Sirens [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:suicidalSirens")) {
            if (attackerFleet.trigger("commander:suicidalSirens")) {
                commanderInstantaneousReload = true;
                commanderAttackPowerBonus += 3.0d;
                commanderReflectBonus += 0.1d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Hellen [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:hellen")) {
            if (attackerFleet.trigger("commander:hellen")) {
                commanderInstantaneousReload = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Penni [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:penni")) {
            if (attackerFleet.trigger("commander:penni")) {
                commanderAttackPowerBonus += 3.0d;
                commanderReflectBonus += 0.1d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Iron Maidens [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:ironMaidens")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                commanderMaximumStability = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Rafia [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:rafia")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                commanderMaximumStability = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Carbuncle Cohort [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:carbuncleCohort")) {
            if (defenderFleet.trigger("commander:carbuncleCohort")) {
                commanderReflectDamageBonus = 0.2;
                commanderDamageReductionBonus += 0.15d;
                commanderEquitationDamage = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Sylla [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:sylla")) {
            if (defenderFleet.trigger("commander:sylla")) {
                commanderDamageReductionBonus += 0.15d;
                commanderEquitationDamage = true;
            }
        }

        // ! The Ravagers [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:theRavagers")) {
            if (attackerFleet.getEffects().contains(EffectType.ROCKY)) {
                FleetEffect fleetEffect = attackerFleet.getEffects().getEffect(EffectType.ROCKY);
                double value = Math.min(fleetEffect.getValue(), 8);
                commanderAttackPowerBonus += value;
                commanderInstantaneousReload = true;
                attackMeta.sourceSkill = 1;
            }
            if (attackerFleet.trigger("commander:theRavagers")) {
                commanderAttackPowerBonus += 1;
                commanderInstantaneousReload = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Rocky [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:rocky")) {
            if (attackerFleet.getEffects().contains(EffectType.ROCKY)) {
                FleetEffect fleetEffect = attackerFleet.getEffects().getEffect(EffectType.ROCKY);
                double value = Math.min(fleetEffect.getValue(), 8);
                commanderAttackPowerBonus += value;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Hand of Lelantos [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:handOfLelantos")) {
            if (attackCommander.getTotalAccuracy() > defenderCommander.getTotalDodge()) {
                commanderIgnoreDaedalus = true;
                commanderIgnoreAgility = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Jakar [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:jakar")) {
            if (attackCommander.getTotalAccuracy() > defenderCommander.getTotalDodge()) {
                commanderIgnoreDaedalus = true;
                commanderIgnoreAgility = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Deadly Duo [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:deadlyDuo")) {
            if (attackerFleet.trigger("commander:deadlyDuo")) {
                commanderVerticalScatteringBonus += 0.5d;
                commanderHorizontalScatteringBonus += 0.4d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Anna [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:anna")) {
            if (attackerFleet.trigger("commander:anna")) {
                commanderVerticalScatteringBonus += 0.4d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Linda [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:linda")) {
            if (attackerFleet.trigger("commander:linda")) {
                commanderHorizontalScatteringBonus += 0.5d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Victory Roar [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:victoryRoar")) {
            if (attackerFleet.trigger("commander:victoryRoar")) {
                commanderIgnoreDefensiveModulesEnabled = true;
                commanderAttackPowerBonus += 3.0d;
                commanderReflectBonus += 0.1d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Fairy & Fiend [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:fairyAndFiend")) {
            if (attackerFleet.trigger("commander:fairyAndFiend")) {
                commanderIgnoreDefensiveModulesEnabled = true;
                commanderStructureDamageBasedShieldBonus = attackCommander.getTrigger().getRate() * 0.01;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Eveline [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:eveline")) {
            if (attackerFleet.trigger("commander:eveline")) {
                commanderIgnoreDefensiveModulesEnabled = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Tactical Wizards [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:tacticalWizards")) {
            if (defenderFleet.trigger("commander:tacticalWizards")) {
                commanderIgnoreDamageEnabled = true;
                additionalAttackerHe3Cost *= 5.0d;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Tactical Wizards [Skill] #2
        if (!attackerSilent && attackerFleet.isCommanded("commander:tacticalWizards")) {
            if (attackerFleet.trigger("commander:tacticalWizards")) {
                additionalDefenderHe3Cost *= 5.0d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Erebus Errants [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:erebusErrants")) {
            if (defenderFleet.trigger("commander:erebusErrants")) {
                commanderIgnoreDamageEnabled = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Nick [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:nick")) {
            if (defenderFleet.trigger("commander:nick")) {
                commanderIgnoreDamageEnabled = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Bruce [Skill] #1
        if (!attackerSilent && attackerFleet.isCommanded("commander:bruce")) {
            if (attackerFleet.trigger("commander:bruce")) {
                additionalDefenderHe3Cost *= 4.0d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Bruce [Skill] #2
        if (!defenderSilent && defenderFleet.isCommanded("commander:bruce")) {
            if (defenderFleet.trigger("commander:bruce")) {
                additionalAttackerHe3Cost *= 4.0d;
                attackMeta.targetSkill = 1;
            }
        }

        // ! The Twin Torpedoes [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:theTwinTorpedoes")) {
            commanderCriticalAttackRateBonus += attackCommander.getTrigger().getRate() * 0.01;
            commanderDoubleAttackRateBonus += attackCommander.getTrigger().getRate() * 0.01;
        }

        // ! Gastaf [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:gastaf")) {
            commanderCriticalAttackRateBonus += attackCommander.getTrigger().getRate() * 0.01;
        }

        // ! Todd [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:todd")) {
            if (attackerFleet.trigger("commander:todd")) {
                commanderDoubleAttackRateBonus += attackCommander.getTrigger().getRate() * 0.01;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Homeric Hellions [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:homericHellions")) {
            if (defenderCommander.getTotalDodge() > attackCommander.getTotalAccuracy()) {
                commanderIgnoreScatteringEnabled = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! The Pioneers [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:thePioneers")) {
            if (defenderCommander.isTotalMoreThan(attackCommander)) {
                commanderIgnoreScatteringEnabled = true;
                commanderMaximumStability = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Andrew [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:andrew")) {
            if (defenderCommander.getTotalDodge() > attackCommander.getTotalAccuracy()) {
                commanderIgnoreScatteringEnabled = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Taude [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:taude")) {
            if (attackerFleet.trigger("commander:taude")) {
                commanderAccuracyBonus += Math.max(attackCommander.getTotalAccuracy() * .5, 0);
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Evi [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:evi")) {
            if (shipAttack.isDirectional()) {
                commanderAttackPowerBonus += attackerFleet.getBattleCommander().getTrigger().getRate() * 0.01;
            }
        }

        // ! Tyren [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:tyren")) {
            if (shipAttack.isMissile()) {
                commanderAttackPowerBonus += attackerFleet.getBattleCommander().getTrigger().getRate() * 0.01;
            }
        }

        // ! Lawrence [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:lawrence")) {
            if (shipAttack.isBallistic()) {
                commanderAttackPowerBonus += attackerFleet.getBattleCommander().getTrigger().getRate() * 0.01;
            }
        }

        // ! Essido [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:essido")) {
            if (shipAttack.isShipBased()) {
                commanderAttackPowerBonus += attackerFleet.getBattleCommander().getTrigger().getRate() * 0.01;
            }
        }

        // ! Frontline Surge [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:frontlineSurge")) {
            if (steps > 0) {
                attackerFleet.setAnimation(true);
                commanderAttackPowerBonus += steps * 0.1;
            }
        }

        // ! Lynn [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:lynn")) {
            if (steps > 0) {
                attackerFleet.setAnimation(true);
                commanderAttackPowerBonus += steps * 0.1;
            }
        }

        // ! Panis [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:panis")) {
            if (shipAttack.canBeIntercepted()) {
                commanderInterceptionRateBonus += defenderFleet.getBattleCommander().getTrigger().getRate() * 0.01;
            }
        }

        // ! Alicia [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:alicia")) {

            FleetEffect fleetEffect = trigger.fetchAndRemove(EffectType.ALICIA, attackerFleet);
            if (fleetEffect != null) {
                commanderAttackPowerBonus += 0.3;
            }

            if (attackerFleet.trigger("commander:alicia")) {
                trigger.setEffect(attackerFleet, EffectType.ALICIA, -1, round);
                attackMeta.sourceSkill = 1;
            }

        }

        // ! Fatal Furies [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:fatalFuries")) {
            if (attackerFleet.trigger("commander:fatalFuries")) {

                commanderBreakthroughEnabled = true;
                commanderAttackPowerBonus += 2.0;

                attackMeta.sourceSkill = 1;

            }
        }

        // ! Feral Raptors [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:feralRaptors")) {
            if (attackerFleet.trigger("commander:feralRaptors")) {
                commanderAttackPowerBonus += 6.0d;
                commanderReflectBonus += 0.1d;
                attackMeta.sourceSkill = 1;
            }
        }

        if (defenderFleet.getEffects().contains(EffectType.LUNA_SILVESTRI)) {
            commanderMaximumStability = true;
            commanderFormationPenaltyDisabled = true;
        }

        // ! Chrome Dome [Skill] #2
        if (!attackerSilent && attackerFleet.getEffects().contains(EffectType.CHROME_DOME)) {
            commanderAttackPowerBonus += 10.0d;
            commanderOverallScatteringBonus += 0.2d;
        }

        // ! Slayer Bael [Skill] #2
        if (attackerFleet.getEffects().contains(EffectType.SLAYER_BAEL)) {
            commanderAttackPowerBonus += 5.0d;
        }

        // ! Murphy Lawson [Skill] #2
        if (!attackerSilent && attackerFleet.getEffects().contains(EffectType.MURPHY_LAWSON)) {
            commanderAttackPowerBonus += 8.0d;
            commanderIgnoreDaedalus = true;
        }

        // ! Wayne [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:wayne")) {
            if (attackerFleet.trigger("commander:wayne")) {
                commanderAttackPowerBonus += 2.0d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Impending Doom [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:impendingDoom")) {
            if (defenderFleet.trigger("commander:impendingDoom")) {
                commanderIgnoreDamageEnabled = true;
                attackMeta.setAttackerEffect(attacker, EffectType.JEROME, 0.1d, -1);
                attackMeta.targetSkill = 1;
            }
        }

        // ! Jerome [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:jerome")) {
            if (defenderFleet.trigger("commander:jerome")) {
                attackMeta.setAttackerEffect(attacker, EffectType.JEROME, 0.1d, -1);
                attackMeta.targetSkill = 1;
            }
        }

        if (defenderCellEffects.contains(EffectType.JEROME)) {

            ShipEffect jeromeEffect = defenderCellEffects.getEffect(EffectType.JEROME);
            commanderAttackPowerBonus += jeromeEffect.getValue();

        }

        // ! Fearmongers [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:fearmongers")) {
            if (defenderFleet.trigger("commander:fearmongers")) {
                attackMeta.addAttackerEffect(attacker, EffectType.RASLIN, 0.5, round + 1);
                additionalAttackerHe3Cost *= 5.0d;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Fearmongers [Skill] #2
        if (!attackerSilent && attackerFleet.isCommanded("commander:fearmongers")) {
            if (attackerFleet.trigger("commander:fearmongers")) {
                additionalDefenderHe3Cost *= 5.0d;
                attackMeta.sourceSkill = 1;
            }
        }

        // ! Raslin [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:raslin")) {
            if (defenderFleet.trigger("commander:raslin")) {
                attackMeta.addAttackerEffect(attacker, EffectType.RASLIN, 0.5, round + 1);
                attackMeta.targetSkill = 1;
            }
        }

        if (attackerCellEffects.contains(EffectType.RASLIN)) {

            ShipEffect effect = attackerCellEffects.getEffect(EffectType.RASLIN);
            commanderAttackPowerBonus -= effect.getValue();

        }

        // ! Joseph [Skill] #1
        if (!defenderSilent && defenderFleet.isCommanded("commander:joseph")) {
            if (defenderCommander.getTotalElectron() > attackCommander.getTotalElectron()) {
                commanderReflectDamageBonus = 0.2;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Indomitable Duo [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:indomitableDuo")) {
            if (defenderCommander.getTotalDodge() > attackCommander.getTotalAccuracy()) {

                commanderStabilityRateBonus += defenderFleet.getBattleCommander().getTrigger().getRate() * 0.01;

                commanderFormationPenaltyDisabled = true;
                commanderIgnoreScatteringEnabled = true;

                attackMeta.targetSkill = 1;

            }
        }

        // ! Heloyce [Skill]
        if (!defenderSilent && defenderFleet.isCommanded("commander:heloyce")) {
            if (defenderFleet.trigger("commander:heloyce")) {
                commanderStabilityRateBonus += defenderFleet.getBattleCommander().getTrigger().getRate() * 0.01;
                commanderFormationPenaltyDisabled = true;
                attackMeta.targetSkill = 1;
            }
        }

        // ! Motima [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:motima")) {
            if (attackerFleet.trigger("commander:motima")) {
                commanderHighestHitRateEnabled = true;
                attackMeta.sourceSkill = 1;
            }
        }

        // ? Chips Variables
        double additionalDaedalus = defenderCommander.getAdditionalDaedalus();
        double additionalStability = defenderCommander.getAdditionalStability();
        double additionalAbsorption = defenderCommander.getAdditionalAbsorption();

        // ? Get defender armor and shield
        double defenderArmor = defenderCell.getStructure();
        double defenderShield = defenderCell.getShields();


        // ? Get ship stats
        ShipStats attackerStats = new ShipStats();
        ShipStats defenderStats = new ShipStats();

        // ? Ship models effects aggregation
        attackerStats.pass(attackerFleet);
        attackerStats.pass(attackerCell.getModel());

        defenderStats.pass(defenderFleet);
        defenderStats.pass(defenderCell.getModel());

        // ? Apply some commander stats to the passed stats
        defenderStats.setStabilityBonus(commanderStabilityRateBonus + defenderStats.getStabilityBonus() - attackerStats.getAllStabilityReduce() + additionalStability);

        // ? Apply commander ignore defensive modules
        if (commanderIgnoreDefensiveModulesEnabled) {

            defenderDefensiveModules = new ArrayList<>();
            interceptModules = new ArrayList<>();

        }

        // * [Step 0. Total damage mitigation] (All types) - Calculate all damage
        // * that can be negated by the shields and structure and save in a
        // * data structure for be used to know how many modules should use.
        ShipDefense shipDefense = new ShipDefense();
        shipDefense.pass(defenderDefensiveModules, attackModules);
        List<BattleFleetAuxiliaryModule> auxiliaryModules = target.getBattleFleetCell().getAuxiliaryModules();

        LinkedHashMap<Integer, List<BattleFleetDefensiveModule>> mappedMitigations = defenderDefensiveModules.stream()
                .filter(module -> module.getTotalNegation(attackModules) > 0)
                .collect(Collectors.groupingBy(ShipModule::getModuleId, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<BattleFleetDefensiveModule>> mitigateModule : mappedMitigations.entrySet()) {

            ShipMitigation shipMitigation = new ShipMitigation();

            for (BattleFleetDefensiveModule defensiveModule : mitigateModule.getValue()) {

                double baseNegation = defensiveModule.getDamageNegate();

                Optional<Double> partBonus = auxiliaryModules.stream().map(x -> x.getDamageNegate(defensiveModule.getPartName())).reduce(Double::sum);
                if (partBonus.isPresent()) {
                    baseNegation += partBonus.get();
                }
                Double shipPartBonus = defenderStats.getPartBonus().getOrDefault(defensiveModule.getPartName(), 0.0d);
                baseNegation += shipPartBonus;

                double kindNegation = defensiveModule.getNegation(attackModules);

                shipMitigation.add(defensiveModule, baseNegation, attackModules, kindNegation);

            }

            if (shipMitigation.getReference().isShield()) {

                shipDefense.getShieldMitigations().add(shipMitigation);
                continue;

            }

            shipDefense.getArmorMitigations().add(shipMitigation);

        }

        // * [Step 0.1. Calculate current interceptions] - Calculate the interceptions
        // * that will be tried.
        // * based on: https://media.discordapp.net/attachments/948323353830252544/952329790877224971/Interception.png
        List<ShipInterception> defenderInterceptions = new ArrayList<>();

        LinkedHashMap<Integer, List<BattleFleetDefensiveModule>> mappedInterceptions = interceptModules.stream()
                .filter(module -> module.getModuleSubType().equals("shipBased") || (module.getModuleSubType().equals("intercept") && (module.isInterceptingShipBased() && shipAttack.isShipBased()) || (module.isInterceptingMissiles() && shipAttack.isMissile())))
                .collect(Collectors.groupingBy(module -> module.getModuleId(), LinkedHashMap::new, Collectors.toList()));

        if (shipAttack.canBeIntercepted()) {

            for (Map.Entry<Integer, List<BattleFleetDefensiveModule>> interceptionModule : mappedInterceptions.entrySet()) {

                ShipInterception shipInterception = new ShipInterception();

                BattleFleetDefensiveModule module = interceptionModule.getValue().get(0);
                if (module == null) {
                    continue;
                }

                double intercept;

                if (module.getModuleSubType().equals("shipBased")) {
                    if (defenderTechs.getShipBasedCustomInterceptRate() > 0) {
                        for (BattleFleetDefensiveModule defensiveModule : interceptionModule.getValue()) {
                            defensiveModule.setIntercept(defenderTechs.getShipBasedCustomInterceptRate());
                        }
                        intercept = defenderTechs.getShipBasedCustomInterceptRate();
                    } else {
                        continue;
                    }
                } else {
                    intercept = module.getIntercept();
                }

                shipInterception.add(interceptionModule.getValue(), intercept);
                defenderInterceptions.add(shipInterception);

            }

            shipDefense.setShipInterceptions(defenderInterceptions);

        }

        // * [Step 0.2. Calculate each module stack] - Calculate the
        // * values for each attack module stack.
        List<ShipDamage> damages = new ArrayList<>();

        LinkedHashMap<Integer, List<BattleFleetAttackModule>> mappedAttacks = attackModules.stream()
                .collect(Collectors.groupingBy(ShipModule::getModuleId, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<BattleFleetAttackModule>> attackModule : mappedAttacks.entrySet()) {

            ShipDamage shipDamage = new ShipDamage();

            for (BattleFleetAttackModule module : attackModule.getValue()) {
                shipDamage.add(module);
            }

            shipDamage.calculate(attackerEffectiveStack);
            damages.add(shipDamage);

        }

        // * [Step 0.3. List necessary attack modules] - List all attack modules
        // * needed for destroy the whole defenderStack (this will take into account
        // * negation and interception).
        boolean canIntercept = shipAttack.canBeIntercepted();

        double virtualDamage = 0;

        double virtualInterceptions = 0;

        double virtualShieldMitigation = 0;
        double virtualArmorMitigation = 0;

        double trueReflectionDamage = 0;
        double trueShieldDamage = 0;
        double trueArmorDamage = 0;

        double attackerHe3 = attackerFleet.getHe3();
        double defenderHe3 = defenderFleet.getHe3();

        boolean stackCritical = false;
        boolean stackDouble = false;

        double attackerFlankRate = attackerStats.getFlankRate();

        double attackerHitRateBonus = attackerStats.getHitRate() + attackerStats.getHitRateBonus();
        double attackerSteeringBonus = 0.0d;
        double attackerDamageBonus = attackerStats.getDamageBonus();

        double defenderOverallNegationRate = defenderStats.getAllDamageReduce();
        double defenderOverallReflectRate = defenderStats.getAllReflectBonus();
        double defenderOverallMitigationBonus = defenderStats.getAllNegatePartBonus();
        boolean defenderFormationPenaltyDisabled = defenderStats.isFlankIgnore();
        double defenderDamageReduceRate = defenderStats.getDamageReduce();
        double defenderCriticalRateReduce = defenderStats.getCriticalRateReduce();

        if (attackerCellEffects.contains(EffectType.RADIATIVE_INTERFERENCE)) {
            attackerHitRateBonus -= attackerCellEffects.getEffect(EffectType.RADIATIVE_INTERFERENCE).getValue();
            attackMeta.removeAttackerEffect(attacker, EffectType.RADIATIVE_INTERFERENCE);
        }

        if (attackerCellEffects.contains(EffectType.ELECTRONIC_INTERFERENCE)) {
            attackerSteeringBonus -= attackerCellEffects.getEffect(EffectType.ELECTRONIC_INTERFERENCE).getValue();
            attackMeta.removeAttackerEffect(attacker, EffectType.ELECTRONIC_INTERFERENCE);
        }


        // ? Based on: https://galaxyonlineii.fandom.com/wiki/Combat_Mechanics#hit_chance_formula
        // ? Next we will follow all of these steps for make the combat calculation

        // ? Activate counterattack modules
        ///get Defender reflective Bonus
        Map<String, Double> partBonus = defenderStats.getPartBonus();

        for (BattleFleetDefensiveModule defensiveModule : defenderDefensiveModules) {

            if (defenderUsage.isUsed(defensiveModule)) {
                continue;
            }
            double reflection = defensiveModule.getDamageReflection();
            if (reflection <= 0) {
                continue;
            }


            if (defenderHe3 < defensiveModule.getFuelUsage() * defenderEffectiveStack) {
                continue;
            }

            defenderHe3 = Math.max(defenderHe3 - (defensiveModule.getFuelUsage() * defenderEffectiveStack), 0);
            Double reflectBonus = 1.0d;
            if (partBonus != null && partBonus.containsKey(defensiveModule.getPartName())) {
                reflectBonus = partBonus.get(defensiveModule.getPartName());
            }
            trueReflectionDamage += defenderEffectiveStack * (reflection + (reflection * reflectBonus));
            defenderUsage.add(defensiveModule, 100, 0);

        }

        boolean rearReduceStability = false;
        boolean shieldsDown = defenderShield <= 0;

        // ? Load attacker modules
        ListIterator<ShipDamage> damageIterator = damages.listIterator();

        // ? Load defender modules
        ListIterator<ShipInterception> interceptIterator = shipDefense.getShipInterceptions().listIterator();
        ListIterator<ShipMitigation> shieldIterator = shipDefense.getShieldMitigations().listIterator();
        ListIterator<ShipMitigation> armorIterator = shipDefense.getArmorMitigations().listIterator();

        // ? Scattering configurations
        ScatteringData scatteringData = new ScatteringData();
        boolean defenderHigherStructure = defenderFleet.getTotalStructure() >= attackerFleet.getTotalStructure();

        // ? Custom items configurations
        if (commanderBreakthroughEnabled) {
            shieldIterator = Collections.emptyListIterator();
        }
        if (commanderIgnoreDamageEnabled || commanderReflectEverything) {
            shipDefense.getShipFacilities().clear();
        }

        Optional<ShipFacility> optionalQRAFacility = shipDefense.getFacility("part:quickReactionArmor");
        Optional<ShipFacility> optionalDaedalusFacility = shipDefense.getFacility("part:daedalusControlSystem");
        Optional<ShipFacility> optionalReflectingFacility = shipDefense.getFacility("part:reflectivePlating");
        Optional<ShipFacility> optionalICSFacility = shipDefense.getFacility("part:icarusControlSystem");

        if (optionalQRAFacility.isPresent() && optionalQRAFacility.get().reference().getFuelUsage() > defenderHe3) {
            optionalQRAFacility = Optional.empty();
        }

        if (optionalDaedalusFacility.isPresent() && optionalDaedalusFacility.get().reference().getFuelUsage() > defenderHe3) {
            optionalDaedalusFacility = Optional.empty();
        }

        if (optionalReflectingFacility.isPresent() && optionalReflectingFacility.get().reference().getFuelUsage() > defenderHe3) {
            optionalReflectingFacility = Optional.empty();
        }

        if (optionalICSFacility.isPresent() && optionalICSFacility.get().reference().getFuelUsage() > defenderHe3) {
            optionalICSFacility = Optional.empty();
        }

        double lastTrueShieldDamage = 0;
        double lastTrueArmorDamage = 0;
        while ((defenderShield > trueShieldDamage || defenderArmor > trueArmorDamage) && damageIterator.hasNext()) {

            ShipDamage shipDamage = damageIterator.next();
            BattleFleetAttackModule reference = shipDamage.getReference();
            String attackType = shipDamage.getAttackType();
            String damageType = shipDamage.getDamageType();
            double attackerCritRate = attackerStats.getCriticalRate();
            double attackerCritRateBonus = attackerStats.getCriticalRateBonus() + attackerStats.getAllCritRate();
            attackerCritRate += attackerTechs.getCriticalRateBonus(shipAttack.getWeaponSubType());
            double additionalMinAttack = attackCommander.getAdditionalMinAttack(attackType);
            double additionalMaxAttack = attackCommander.getAdditionalMaxAttack(attackType);
            double defenderDaedalusRateBonus = 0.0d;
            boolean highestBaseHitRateEnabled = commanderHighestHitRateEnabled;
            double defenderAgilityBonus = 0.0d;
            double ccAttackPowerBonus = commanderAttackPowerBonus;

            //System.out.println("weapon chip buff: " + additionalMinAttack + ":" + additionalMaxAttack + " with rate " + chipDamageEffectiveRate);
            double attackerDoubleRate = attackerStats.getDoubleRate() + attackerStats.getAllDoubleRate();
            if (steps > 0 && attackType.equals("directional")) {
                attackerCritRateBonus += attackerTechs.getLaserCriticalRateBonus();
            }
            if (attackType.equals("directional") && attackerTechs.getLaserIncreaseMaximumHitRateChance() > 0) {
                if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getLaserIncreaseMaximumHitRateChance() * 100.0d) {
                    highestBaseHitRateEnabled = true;
                }
            }

            if (attackType.equals("directional") && attackerTechs.getLaserIgnoreAgilityChance() > 0) {
                if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getLaserIgnoreAgilityChance() * 100.0d) {
                    defenderAgilityBonus -= attackerTechs.getLaserIgnoreAgilityRate();
                }
            }

            if (attackType.equals("directional") && attackerTechs.getLaserReduceDefenseRateBonus() > 0) {
                defenderDaedalusRateBonus -= attackerTechs.getLaserReduceDefenseRateBonus();
            }
            double weaponHe3Cost = shipDamage.getFuelUsage(1);

            if (attackType.equals("missile")) {
                weaponHe3Cost = Math.max(weaponHe3Cost - (weaponHe3Cost * attackerTechs.getMissileHE3ReductionRateBonus()), 0);
                if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getMissileAdditionalHE3ReductionChance() * 100.0d) {
                    weaponHe3Cost = Math.max(weaponHe3Cost - (weaponHe3Cost * attackerTechs.getMissileAdditionalHE3ReductionValue()), 0);
                }
            }

            if (attackType.equals("shipBased")) {
                weaponHe3Cost = Math.max(weaponHe3Cost - (weaponHe3Cost * attackerTechs.getShipBasedHE3ReductionRateBonus()), 0);
                if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getShipBasedImproveHitChance() * 100.0d) {
                    weaponHe3Cost = Math.max(weaponHe3Cost + (weaponHe3Cost * attackerTechs.getShipBasedImproveHitHe3Cost()), 0);
                    ccAttackPowerBonus += attackerTechs.getShipBasedImproveHitDamageBonus();
                }
            }

            double weaponEffectiveHe3Cost = weaponHe3Cost * attackerEffectiveStack;

            if (attackerHe3 < weaponEffectiveHe3Cost) {
                continue;
            }

            // ? Max Range
            int maxRange = shipDamage.getReference().getMaxRange(attackerTechs);

            // ? Set attack basics
            attackMeta.setAttack(true);
            attackerHe3 = Math.max(attackerHe3 - weaponEffectiveHe3Cost, 0);

            // ? Reload Reduction
            int reloadReduction = 0;

            if (attackType.equals("missile")) {
                reloadReduction += attackerTechs.getMissileCooldownReduction();
            }
            if (attackType.equals("shipBased")) {
                reloadReduction += attackerTechs.getShipBasedCooldownReduction();
                if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getShipBasedInstantReloadChance() * 100.0d) {
                    reloadReduction = 10000;
                }
            }
            if (attackType.equals("ballistic")) {
                reloadReduction = 10000;
            }

            // ? Weapon reload
            if (commanderInstantaneousReload) {
                reloadReduction = 10000;
            }
            shipDamage.shoot(matchRunnable.getMatch().getRound() + 1 - reloadReduction);

            // ? Calculate hit rate
            // ? Tech can add hit rate bonus
            // ? Agility decrease hit chance (1 agility decreases 4%)
            // ? Steering increase hit chance (1 steering increases 4%)
            // ? Each 12 dodge reduce hit chance by 1%
            // ? Each 12 accuracy increase hit chance by 1%
            double steeringBonus = attackerTechs.getSteeringPowerBonus(shipAttack.getWeaponSubType()) + attackerSteeringBonus;
            double steering = attackerStats.getSteering() + (shipDamage.getSteering() * (1.0d + steeringBonus));

            double agility = defenderStats.getAgility() * (1 + defenderTechs.getShipAgilityBonus() + defenderAgilityBonus);
            if (commanderIgnoreAgility) {
                agility = 0;
            }

            double hitRateBase = shipDamage.getReference().getHitRate();
            double hitRateBonus = attackerTechs.getHitRateBonus(shipAttack.getWeaponSubType());

            double statsHitModifier;
            double commHitModifier;

            if (highestBaseHitRateEnabled) {
                statsHitModifier = (steering) * 0.04;
                commHitModifier = (((attackCommander.getTotalAccuracy() + commanderAccuracyBonus) / 12)) * 0.01;
            } else {
                statsHitModifier = (steering - agility) * 0.04;
                commHitModifier = (((attackCommander.getTotalAccuracy() + commanderAccuracyBonus) / 12) - (defenderCommander.getTotalDodge() / 12)) * 0.01;
            }


            double hitRate = Math.max(hitRateBase + hitRateBonus + statsHitModifier + commHitModifier + attackerHitRateBonus, 0);

            // ? Add attack usage with the hit rate
            shipDamage.addUsage(attackerUsage, hitRate);

            // ? Commander is ignoring the damage
            if (commanderIgnoreDamageEnabled) {
                continue;
            }

            // ? Intercept if is possible
            double originalHits = shipDamage.getHits();
            double shipHits = originalHits * hitRate;

            // ? Penetration
            double penetration = Math.max(attackerStats.getPierceShield() + attackerStats.getAllPierceShield() + commanderPierceBonus + attackerTechs.getPenetration(shipAttack.getWeaponSubType()) - defenderTechs.getEnemyShieldPenetrationReductionRateBonus(), 0);

            // ? Scattering / Piercing
            double scattering = attackerTechs.getScattering(shipAttack.getWeaponSubType());
            // ? Expertise Modifier
            double attackerWeaponExpertiseBonus = attackCommander.getExpertise().getWeaponModifier(reference.getModuleSubType());
            double attackerExpertiseDealtBonus = attackCommander.getExpertise().getShipDamageModifier(attackerStats.getBodyType());
            double defenderExpertiseDefendBonus = defenderCommander.getExpertise().getShipDamageReductionModifier(defenderStats.getBodyType());
            // ? Damages based in Armor/Shield
            if (attackType.equals("ballistic") && attackerTechs.getBallisticDamageOnHpOrLess() > 0.0d) {
                double minDurability = (defenderCell.getMaxDurability()) * attackerTechs.getBallisticDamageOnHpOrLess();
                double durability = defenderCell.getDurability();
                if (durability <= minDurability) {
                    attackerExpertiseDealtBonus += attackerTechs.getBallisticDamageOnHpHit();
                }
            }

            // ? Calculate damages
            double attackPowerBonus = 1 + (ccAttackPowerBonus + attackerWeaponExpertiseBonus + attackerExpertiseDealtBonus + attackerDamageBonus + defenderExpertiseDefendBonus);

            if (attackType.equals("missile") && attackerTechs.getMissileAdditionalIncreaseDamageChance() > 0) {
                if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getMissileAdditionalIncreaseDamageChance() * 100.0d) {
                    attackPowerBonus += attackerTechs.getMissileAdditionalIncreaseDamageValue();
                }
            }

            if (attackType.equals("shipBased")) {
                if (shieldsDown) {
                    attackPowerBonus += attackerTechs.getShipBasedEnemyNoShieldIncreaseDamageBonus();
                } else {
                    attackPowerBonus += attackerTechs.getShipBasedEnemyWithShieldIncreaseDamageBonus();
                }
            }

            if (attackerCellEffects.contains(EffectType.PARTICLE_IMPACT_TECH)) {
                attackPowerBonus -= Math.max(attackerCellEffects.getEffect(EffectType.PARTICLE_IMPACT_TECH).getValue(), 0);
            }

            // ? Distance based damages
            if (attackType.equals("ballistic")) {
                switch (distance) {
                    case 1:
                        if (maxRange > 2) {
                            attackPowerBonus += attackerTechs.getBallisticRange1IncreaseDamageRateBonus();
                        } else {
                            attackPowerBonus += attackerTechs.getBallisticRange3IncreaseDamageRateBonus();
                        }
                        break;
                    case 2:
                        if (maxRange > 2) {
                            attackPowerBonus += attackerTechs.getBallisticRange2IncreaseDamageRateBonus();
                        } else {
                            attackPowerBonus += attackerTechs.getBallisticRange4IncreaseDamageRateBonus();
                        }
                        break;
                    case 3:
                        attackPowerBonus += attackerTechs.getBallisticRange3IncreaseDamageRateBonus();
                        break;
                    case 4:
                        attackPowerBonus += attackerTechs.getBallisticRange4IncreaseDamageRateBonus();
                        break;
                }
            }

            if (attackType.equals("shipBased")) {
                switch (distance) {
                    case 6:
                        attackPowerBonus += attackerTechs.getShipBasedLongRangedAway6SlotsIncreaseBaseDamage();
                        break;
                    case 7:
                        attackPowerBonus += attackerTechs.getShipBasedLongRangedAway7SlotsIncreaseBaseDamage();
                        break;
                    case 8:
                        attackPowerBonus += attackerTechs.getShipBasedLongRangedAway8SlotsIncreaseBaseDamage();
                        break;
                    case 9:
                        attackPowerBonus += attackerTechs.getShipBasedLongRangedAway9SlotsIncreaseBaseDamage();
                        break;
                    default:
                        if (distance < 10) {
                            break;
                        }
                        attackPowerBonus += attackerTechs.getShipBasedLongRangedAway10SlotsIncreaseBaseDamage();
                        attackerDoubleRate += attackerTechs.getShipBasedLongRangedAwayDoubleChance();
                        break;
                }
            }

            // ? Calculate double attack and critical damage
            double stackCritRate = Math.min((attackerCritRate + attackerCritRateBonus + commanderCriticalAttackRateBonus + attackerElectronBonus + shipDamage.getReference().getCritRate() - defenderCriticalRateReduce) * 100.0d, 100.0d);
            boolean moduleCritical = stackCritRate > 0 && RandomUtil.getRandomInt(0, 100) <= stackCritRate;

            double stackDoubleRate = Math.min((attackerDoubleRate + commanderDoubleAttackRateBonus) * 100.0d, 100.0d);
            boolean moduleDouble = stackDoubleRate > 0 && RandomUtil.getRandomInt(0, 100) <= stackDoubleRate;

            if (moduleCritical && commanderTriggerBlast) {

                fullCritical = true;
                fullDouble = true;

            } else if (moduleDouble && commanderTriggerBlast) {

                fullCritical = true;
                fullDouble = true;

            }

            if (fullCritical) {

                moduleCritical = true;

                if (attackerFleet.hasRobert()) {
                    attackMeta.sourceSkill = 1;
                }

            }

            if (fullDouble) {

                moduleDouble = true;

                if (attackerFleet.hasRobert()) {
                    attackMeta.sourceSkill = 1;
                }

            }

            if (moduleCritical) {
                stackCritical = true;
            }
            if (moduleDouble) {
                stackDouble = true;
            }

            int minAttack = (int) (reference.getMinAttack() + additionalMinAttack);
            int maxAttack = (int) (reference.getMaxAttack() + additionalMaxAttack);

            // System.out.println("MinAttack: " + minAttack + " ReferenceMinAttack: " + reference.getMinAttack() + " AdditionalMinAttack: " + additionalMinAttack);

            // ? Apply daedalus reduction (Formula
            // ? extracted from krtools)
            double daedalus = defenderStats.getDaedalus() + defenderCommander.getTemporalDefenseRate() + additionalDaedalus;

            // Apply Daedalus Bonus and Calculate Damage
            daedalus = daedalus * (1 + defenderTechs.getShipDaedalusBonus() + defenderDaedalusRateBonus);
            if (commanderIgnoreDaedalus) {
                daedalus = 0;
            }

            interceptLoop:
            while (!commanderReflectEverything && canIntercept && virtualInterceptions < shipHits && interceptIterator.hasNext()) {

                ShipInterception interception = interceptIterator.next();

                double fuelUsage = interception.getFuelUsage(1);
                if (defenderHe3 < (fuelUsage * defenderEffectiveStack)) {
                    continue;
                }

                double referenceInterception = reference.getIntercept() + attackerTechs.getAttackInterceptRateBonus(shipAttack.getWeaponSubType());

                defenderHe3 = Math.max(defenderHe3 - (fuelUsage * defenderEffectiveStack), 0);
                Double shipPartBonus = defenderStats.getPartBonus().getOrDefault(interception.getReference().getPartName(), 0.0d);
                virtualInterceptions += (interception.getInterceptionRate() + commanderInterceptionRateBonus + shipPartBonus) * referenceInterception * defenderEffectiveStack * interception.getInterceptors().size();

                // check h3 overconsumption
                if (virtualInterceptions >= shipHits) {

                    double oneInterception = (interception.getInterceptionRate() + commanderInterceptionRateBonus) * referenceInterception * 1 * interception.getInterceptors().size();
                    double checkInterception = virtualInterceptions;

                    while (checkInterception > shipHits) {
                        checkInterception = Math.max(checkInterception - oneInterception, 0);
                        defenderHe3 += fuelUsage;
                    }

                    //virtualInterceptions = shipHits;
                    defenderHe3 = Math.max(defenderHe3 - fuelUsage, 0);

                }

                interception.addUsage(defenderUsage);

            }

            virtualInterceptions *= hitRate;
            if (canIntercept && virtualInterceptions > 0) {
                double hits = shipHits;
                shipHits = Math.max(hits - virtualInterceptions, 0);
                virtualInterceptions = Math.max(virtualInterceptions - hits, 0);
            }
            double calculatedRandomDamage = RandomUtil.getSmartRandomInt(minAttack, maxAttack);
            double calculatedDamage = Math.max(calculatedRandomDamage * attackPowerBonus * shipHits, 0) + additionalNetDamage;
            virtualDamage = calculatedDamage;
            // System.out.println("[" + attackerFleet.getGuid() + "] ["+ reference.getData().getName() +"] Calculated Damage: " + virtualDamage + ", Random Damage: " + calculatedRandomDamage + ", minAttack: " + minAttack + ", maxAttack: " + maxAttack + ", attackPowerBonus: " + attackPowerBonus + ", shipHits: " + shipHits + ", additionalNetDamage: " + additionalNetDamage);
            // ? Add tech damage bonus
            virtualDamage += (virtualDamage * attackerTechs.getDamageDealtBonus(reference.getModuleSubType()));

            // ? Add damage bonus
            double criticalDamageBonus = commanderCriticalAttackDamageBonus + attackerTechs.getCriticalDamageBonus(reference.getModuleSubType()) + attackerElectronBonus - defenderTechs.getEnemyCriticalStrikeDamageReduction();
            // ? Apply critical and doubles
            if (moduleCritical) {
                virtualDamage += Math.max(virtualDamage * (0.35 + criticalDamageBonus), 0);
            }
            if (moduleDouble) {
                virtualDamage += virtualDamage;
            }

            // Daedalus Facility #1
            if (trueArmorDamage > 0 && optionalDaedalusFacility.isPresent()) {
                ShipFacility daedalusFacility = optionalDaedalusFacility.get();
                daedalus += daedalusFacility.getEffect("daedalus");
                daedalusFacility.addUsage(defenderUsage);
            }

            // Icarus Facility #1
            if (trueArmorDamage > 0 && optionalICSFacility.isPresent()) {
                ShipFacility icarusFacility = optionalICSFacility.get();
                daedalus += icarusFacility.getEffect("daedalus");
                icarusFacility.addUsage(defenderUsage);
            }

            // ? Damage reduction/bonuses
            // ? based on armor type
            switch (defenderStats.getArmorType()) {

                case "chrome":

                    switch (damageType) {
                        case "explosive": // nano x explosive 50% addition
                            virtualDamage += virtualDamage * 0.5;
                            break;
                        case "magnetic": // nano x magnetic 0% reduction
                            break;
                        case "kinetic": // nano x kinetic 25% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.25), 0);
                            break;
                        case "heat": // nano x heat 50% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.5), 0);
                            break;
                    }

                    if (attackType.equals("ballistic")) {
                        virtualDamage += virtualDamage * attackerTechs.getBallisticDamageAgainstChromeRate();
                    }
                    virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderTechs.getArmorTypeChromeDamageReductionBonus()), 0);
                    break;

                case "nano":

                    switch (damageType) {
                        case "explosive": // nano x explosive 50% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.5), 0);
                            break;
                        case "magnetic": // nano x magnetic 50% addition
                            virtualDamage += virtualDamage * 0.5;
                            break;
                        case "kinetic": // nano x kinetic 0% reduction
                            break;
                        case "heat": // nano x heat 25% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.25), 0);
                            break;
                    }

                    if (attackType.equals("ballistic")) {
                        virtualDamage += virtualDamage * attackerTechs.getBallisticDamageAgainstNanoRate();
                    }
                    virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderTechs.getArmorTypeNanoDamageReductionBonus()), 0);
                    break;

                case "neutralizing":

                    switch (damageType) {
                        case "explosive": // neutralizing x explosive 25% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.25), 0);
                            break;
                        case "magnetic": // neutralizing x magnetic 50% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.5), 0);
                            break;
                        case "kinetic": // neutralizing x kinetic 50% addition
                            double kineticAddition = 0.5;
                            if (attackType.equals("ballistic")) {
                                kineticAddition += attackerTechs.getBallisticKineticDamageAgainstNeutralizingRate();
                            }
                            virtualDamage += virtualDamage * kineticAddition;
                            break;
                        case "heat": // neutralizing x heat 0% reduction
                            break;
                    }

                    if (attackType.equals("ballistic")) {
                        virtualDamage += virtualDamage * attackerTechs.getBallisticDamageAgainstNeutralizingRate();
                    }
                    virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderTechs.getArmorTypeNeutralizingDamageReductionBonus()), 0);
                    break;

                case "regen":

                    switch (damageType) {
                        case "explosive": // regen x explosive 0% reduction
                            break;
                        case "magnetic": // regen x magnetic 25% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.25), 0);
                            break;
                        case "kinetic": // regen x kinetic 50% reduction
                            virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.5), 0);
                            break;
                        case "heat": // regen x heat 50% addition
                            double regenAddition = 0.5;
                            if (attackType.equals("ballistic")) {
                                regenAddition += attackerTechs.getBallisticHeatDamageAgainstRegenRate();
                            }
                            virtualDamage += virtualDamage * regenAddition;
                            break;
                    }

                    if (attackType.equals("ballistic")) {
                        virtualDamage += calculatedDamage * attackerTechs.getBallisticDamageAgainstRegenRate();
                    }
                    virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderTechs.getArmorTypeRegenDamageReductionBonus()), 0);
                    break;

                case "light": // light x any 90% reduction

                    double againstLightDamageBonus = 0.0;
                    double lightReduction = 0.9;
                    if (attackType.equals("ballistic")) {
                        if (damageType.equals("kinetic")) {
                            lightReduction -= attackerTechs.getBallisticKineticDamageAgainstLightRate();
                        } else if (damageType.equals("heat")) {
                            lightReduction -= attackerTechs.getBallisticHeatDamageAgainstLightRate();
                        }
                        lightReduction -= attackerTechs.getBallisticDamageAgainstLightRate();
                    }
                    if (attackerStats.getArmorBonus().containsKey("light")) {
                        againstLightDamageBonus += attackerStats.getArmorBonus().get("light");
                    }

                    // ! Miller [Skill]
                    if (!attackerSilent && attackerFleet.isCommanded("commander:miller")) {
                        againstLightDamageBonus += 3.0d;
                    }

                    lightReduction -= againstLightDamageBonus;

                    virtualDamage = Math.max(virtualDamage - (virtualDamage * lightReduction), 0);

                    virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderTechs.getArmorTypeLightDamageReductionBonus()), 0);
                    break;
            }

            if (defenderDamageReduceRate != 0) {
                virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderDamageReduceRate), 0);
            }


            // ? Apply daedalus (defense) reduction
            virtualDamage = Math.max(virtualDamage - (virtualDamage * (daedalus * 0.03 / (daedalus * 0.03 + 1))), 0);

            // ? Check damage bonuses
            // ? based on formation
            if (!commanderFormationPenaltyDisabled && !defenderFormationPenaltyDisabled) {

                switch (defensiveDirection) {
                    case 0: // Rear
                        rearReduceStability = true;
                        virtualDamage += Math.floor(virtualDamage * 0.1);
                        break;
                    case 1, 3: // Sides
                        virtualDamage += Math.floor(virtualDamage * 0.2);
                        break;
                }

                virtualDamage += (virtualDamage * attackerFlankRate);

                switch (attackMeta.getAttackerSegmentedPosIndex()) {
                    case 1: // Segment 2
                        virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.10), 0);
                        break;
                    case 2: // Segment 3
                        virtualDamage = Math.max(virtualDamage - (virtualDamage * 0.25), 0);
                        break;
                }
            }

            // ? Flagship reduction
            if (defenderOverallNegationRate > 0) {
                virtualDamage = Math.max(virtualDamage - (virtualDamage * defenderOverallNegationRate), 0.0d);
            }

            // ? Check if there is remaining shield mitigation
            double remainingShieldMitigation = Math.max(virtualShieldMitigation - virtualDamage, 0);

            if (remainingShieldMitigation == 0) {

                virtualDamage = Math.max(virtualDamage - virtualShieldMitigation, 0);
                virtualShieldMitigation = 0;

            } else {

                virtualShieldMitigation = remainingShieldMitigation;
                virtualDamage = 0;

            }

            // ? Net damage
            double netDamage = 0;
            double negateRateBonus = commanderShieldNegationBonus;

            // ? Check if has to reflect everything
            if (commanderReflectEverything) {

                trueReflectionDamage += virtualDamage;
                continue;

            }

            // ? Calculate penetration damage
            double penetrationDamage = virtualDamage * penetration;

            // System.out.println("Penetration Damage: " + penetrationDamage + ", Virtual Shield: " + virtualShield + ", Virtual Damage: " + virtualDamage);

            // ? Calculate shield damage mitigation
            while (defenderShield > 0 && virtualDamage > 0 && shieldIterator.hasNext()) {

                ShipMitigation mitigation = shieldIterator.next();

                double fuelUsage = mitigation.getFuelUsage(1);
                fuelUsage = Math.max(fuelUsage - (fuelUsage * defenderTechs.getAllNegationModulesNoHe3ConsumptionRateBonus()), 0);
                if (defenderHe3 < (fuelUsage * defenderEffectiveStack)) {
                    continue;
                }

                mitigation.addUsage(defenderUsage);
                defenderHe3 = Math.max(defenderHe3 - (fuelUsage * defenderEffectiveStack), 0);

                // System.out.println("mitigation count: " + mitigation.count());

                double mitigationIncrement = defenderTechs.getShieldModuleDamageReduction();
                double oneMitigation = mitigation.getTotalMitigation(damageType) + (mitigation.count() * (mitigationIncrement + additionalAbsorption + defenderOverallMitigationBonus));
                double doubleMitigationChance = Math.min(defenderTechs.getModuleDoubleDamageAbsorptionChanceBonus(), 1.0d);
                if (MathUtil.randomInclusive(1, 100) <= (doubleMitigationChance * 100.0d)) {
                    oneMitigation *= 2;
                }

                double shieldMitigation = oneMitigation * defenderEffectiveStack;
                shieldMitigation = Math.max(shieldMitigation * (1 + negateRateBonus), 0);
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
            if (!commanderBreakthroughEnabled) {
                if (virtualDamage >= defenderShield) {
                    if (trueShieldDamage < defenderShield) {
                        trueShieldDamage += defenderShield;
                    } else {
                        trueShieldDamage = defenderShield;
                    }
                    virtualDamage = Math.max(virtualDamage - defenderShield, 0);
                } else if (trueShieldDamage < defenderShield) {
                    trueShieldDamage += virtualDamage;
                    virtualDamage = 0;
                }
            }

            // System.out.println("VirtualDMG: " + virtualDamage + ", HitRate: " + hitRate + ", Hits: " + shipHits);


            // * On-Hit Techs
            if (trueShieldDamage > 0 || trueArmorDamage > 0) {
                if (attackType.equals("directional")) { // Laser techs
                    if (attackerTechs.getLaserRadiativeInterference() > 0) {
                        double current = 0.0d;
                        if (defenderCell.getEffects().contains(EffectType.RADIATIVE_INTERFERENCE)) {
                            current = defenderCell.getEffects().getEffect(EffectType.RADIATIVE_INTERFERENCE).getValue();
                        }
                        if (current < attackerTechs.getLaserRadiativeInterferenceMaximum()) {
                            attackMeta.addDefenderEffect(target, EffectType.RADIATIVE_INTERFERENCE, attackerTechs.getLaserRadiativeInterference(), -1);
                        }
                    }
                    if (attackerTechs.getLaserReduceSteeringPowerChance() > 0 && !defenderCellEffects.contains(EffectType.ELECTRONIC_INTERFERENCE)) {
                        if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getLaserReduceSteeringPowerChance() * 100.0d) {
                            attackMeta.addDefenderEffect(target, EffectType.ELECTRONIC_INTERFERENCE, attackerTechs.getLaserReduceSteeringPowerRate(), -1);
                        }
                    }
                    if (attackerTechs.getLaserFleetMovementReductionChance() > 0 && !defenderFleet.getEffects().contains(EffectType.DYNAMIC_IMPAIRMENT)) {
                        if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getLaserFleetMovementReductionChance() * 100.0d) {
                            trigger.setEffect(defenderFleet, EffectType.DYNAMIC_IMPAIRMENT, attackerTechs.getLaserFleetMovementReductionAmount(), round + 1);
                        }
                    }
                } else if (attackType.equals("missile")) { // Missile techs
                    if (!defenderFallbackTech && MathUtil.randomInclusive(1, 100) <= attackerTechs.getMissileRepelChance() * 100.0d) {
                        trigger.setRepelSteps((int) (trigger.getRepelSteps() + attackerTechs.getMissileRepelRange()));
                        defenderFallbackTech = true;
                    }
                }
            }
            // ? Add penetration damage to the virtual damage
            virtualDamage += penetrationDamage;

            // ? Check if there is remaining armor mitigation
            double remainingArmorMitigation = Math.max(virtualArmorMitigation - virtualDamage, 0);

            if (remainingArmorMitigation == 0) {

                virtualDamage = Math.max(virtualDamage - virtualArmorMitigation, 0);
                virtualArmorMitigation = 0;

            } else {

                virtualArmorMitigation = remainingArmorMitigation;
                virtualDamage = 0;

            }

            // ? Additional damage to structure based on shield damage
            if (virtualDamage > 0) {
                virtualDamage += Math.max(trueShieldDamage * commanderStructureDamageBasedShieldBonus, 0);
            }

            // Icarus Facility #2
            if (defenderArmor > 0 && virtualDamage > 0 && optionalICSFacility.isPresent()) {
                ShipFacility icarusFacility = optionalICSFacility.get();
                negateRateBonus += icarusFacility.getEffect("negateRate");
                icarusFacility.addUsage(defenderUsage);
            }

            // ? Calculate armor damage mitigation
            while (defenderArmor > 0 && virtualDamage > 0 && armorIterator.hasNext()) {

                ShipMitigation mitigation = armorIterator.next();

                double fuelUsage = mitigation.getFuelUsage(1);
                fuelUsage = Math.max(fuelUsage - (fuelUsage * defenderTechs.getAllNegationModulesNoHe3ConsumptionRateBonus()), 0);
                if (defenderHe3 < (fuelUsage * defenderEffectiveStack)) {
                    continue;
                }

                mitigation.addUsage(defenderUsage);
                defenderHe3 = Math.max(defenderHe3 - (fuelUsage * defenderEffectiveStack), 0);

                double oneMitigation = mitigation.getTotalMitigation(damageType) + (mitigation.count() * defenderOverallMitigationBonus);
                double doubleMitigationChance = Math.min(defenderTechs.getModuleDoubleDamageAbsorptionChanceBonus(), 1.0d);
                if (MathUtil.randomInclusive(1, 100) <= (doubleMitigationChance * 100.0d)) {
                    oneMitigation *= 2;
                }

                double armorMitigation = oneMitigation * defenderEffectiveStack;
                armorMitigation = Math.max(armorMitigation * (1 + negateRateBonus), 0);
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
                virtualArmorMitigation = Math.max(virtualArmorMitigation - virtualDamage, 0);
                virtualDamage = 0;

            }

            // Facilities Variables (After Shields down)
            shieldsDown = defenderShield == 0 || trueShieldDamage >= defenderShield;
            double facilitiesDamageReduction = 0.0d;

            // Daedalus Facility #2
            if (trueArmorDamage > 0 && optionalDaedalusFacility.isPresent()) {
                ShipFacility daedalusFacility = optionalDaedalusFacility.get();
                facilitiesDamageReduction += daedalusFacility.getEffect("damageReduce") + defenderTechs.getActivatedDaedalusShipDamageReduction();
                daedalusFacility.addUsage(defenderUsage);
            }

            // QRA Facility #1
            if (optionalQRAFacility.isPresent()) {

                ShipFacility qraFacility = optionalQRAFacility.get();

                if (qraFacility.isReloaded(matchRunnable.getRound() + 1)) {

                    qraFacility.trigger(matchRunnable.getRound() + 1);
                    qraFacility.addUsage(defenderUsage);

                }

                if (qraFacility.isEnabled()) {

                    BattleFleetDefensiveModule qraReference = qraFacility.reference();
                    PartLevelMeta levelMeta = qraReference.getMeta();

                    double qraReduction = (Double) levelMeta.getEffect("damageReduce").getValue() + defenderTechs.getReactionArmorDamageReductionBonus();
                    facilitiesDamageReduction += qraReduction;

                }

            }

            // ? Apply som reductions
            virtualDamage = Math.max(virtualDamage / (1 + facilitiesDamageReduction + commanderDamageReductionBonus), 0);

            // System.out.println("ReductionDMG: " + virtualDamage + ", Armor: " + virtualArmor);

            // ? Apply armor damage with reductions
            if (virtualDamage >= defenderArmor && trueArmorDamage < defenderArmor) {
                trueArmorDamage += defenderArmor;
                virtualDamage = Math.max(virtualDamage - defenderArmor, 0);
            } else if (trueArmorDamage < defenderArmor) {
                trueArmorDamage += virtualDamage;
                virtualDamage = 0;
            }

            // System.out.println("TrueArmorDamage: " + trueArmorDamage);


            // ? Add true damages to the net damage
            ScatteringType scatteringType = ScatteringType.fromAttackType(attackType);
            double nettoShieldDamage = trueShieldDamage - lastTrueShieldDamage;
            double nettoArmorDamage = trueArmorDamage - lastTrueArmorDamage;
            netDamage = commanderIgnoreScatteringEnabled ? 0 : (nettoShieldDamage + nettoArmorDamage);

            // ? Sylla [Skill] #2 & Carbuncle Cohort [Skill] #2
            if (netDamage > 0 && commanderEquitationDamage) {

                int distribution = Long.valueOf(sortedDefenderPositions.stream()
                        .filter(shipPosition -> shipPosition.getBattleFleetCell().hasShips())
                        .count()).intValue();

                trueShieldDamage = Math.max(nettoShieldDamage / distribution, 0);
                trueArmorDamage = Math.max(nettoArmorDamage / distribution, 0);

                netDamage = Math.max(nettoShieldDamage + nettoArmorDamage, 0);
                scatteringType = ScatteringType.DISTRIBUTED;

                attackMeta.targetSkill = 1;

            }

            // ? Apply weapon scattering damage if any
            double flagshipScattering = attackerStats.getAllAttackSplash();
            if (flagshipScattering > 0 && scatteringType == ScatteringType.NONE) {
                scatteringType = ScatteringType.GLOBAL;
            }

            scatter:
            if (netDamage > 0 && (scattering > 0 || flagshipScattering > 0 || commanderVerticalScatteringBonus > 0 || commanderHorizontalScatteringBonus > 0)) {
                double verticalScatteringRate = commanderVerticalScatteringBonus;
                double horizontalScatteringRate = commanderHorizontalScatteringBonus;

                if (attackType.equals("ballistic") && attackerTechs.getBallisticIncreaseDispersionMaximumChance() > 0) {
                    if (MathUtil.randomInclusive(1, 100) <= attackerTechs.getBallisticIncreaseDispersionMaximumChance() * 100.0d) {
                        horizontalScatteringRate += attackerTechs.getBallisticIncreaseDispersionMaximumDamageRateBonus();
                    }
                }

                double baseScatteringDamage = Math.max(netDamage * (flagshipScattering + commanderOverallScatteringBonus), 0);

                double scatteringReduction = defenderTechs.getScatteringDamageReductionRateBonus();

                if ((scatteringType == ScatteringType.VERTICAL)) {
                    if (attackerTechs.isLaserPiercedCriticalStrike() && moduleCritical) {
                        verticalScatteringRate += (0.35 + criticalDamageBonus);
                    }
                }

                for (ShipPosition position : sortedDefenderPositions) {
                    if (position.equals(target)) {
                        continue;
                    }
                    if (!position.getBattleFleetCell().hasShips()) {
                        continue;
                    }
                    double realScatterDamage = 0;
                    switch (scatteringType) {
                        case VERTICAL:
                            if (position.getPosIndex() == target.getPosIndex() &&
                                    MathUtil.isInRange(position.getSegmentedPosIndex(), 0, 2)) {
                                if (attackerTechs.getLaserDecreaseTargetDamageRounds() > 0) {
                                    attackMeta.setDefenderEffect(position,
                                            EffectType.PARTICLE_IMPACT_TECH,
                                            attackerTechs.getLaserDecreaseTargetDamage(),
                                            (int) attackerTechs.getLaserDecreaseTargetDamageRounds() + round);
                                }
                                realScatterDamage = baseScatteringDamage + (netDamage * (verticalScatteringRate + scattering));
                            } else if (horizontalScatteringRate > 0 && position.getSegmentedPosIndex() == target.getSegmentedPosIndex() &&
                                    MathUtil.isInRange(position.getPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + (netDamage * horizontalScatteringRate);
                            } else if (flagshipScattering > 0 || commanderOverallScatteringBonus > 0) {
                                realScatterDamage = baseScatteringDamage;
                            }
                            break;
                        case HORIZONTAL:
                            if (position.getSegmentedPosIndex() == target.getSegmentedPosIndex() &&
                                    MathUtil.isInRange(position.getPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + (netDamage * (horizontalScatteringRate + scattering));
                            } else if (verticalScatteringRate > 0 && position.getPosIndex() == target.getPosIndex() &&
                                    MathUtil.isInRange(position.getSegmentedPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + netDamage * verticalScatteringRate;
                            } else if (flagshipScattering > 0 || commanderOverallScatteringBonus > 0) {
                                realScatterDamage = baseScatteringDamage;
                            }
                            break;
                        case GLOBAL:
                            if (horizontalScatteringRate > 0 && position.getSegmentedPosIndex() == target.getSegmentedPosIndex() &&
                                    MathUtil.isInRange(position.getPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + (netDamage * (horizontalScatteringRate + scattering));
                            } else if (verticalScatteringRate > 0 && position.getPosIndex() == target.getPosIndex() &&
                                    MathUtil.isInRange(position.getSegmentedPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + (netDamage * (verticalScatteringRate + scattering));
                            } else {
                                realScatterDamage = baseScatteringDamage + (netDamage * scattering);
                            }
                            if (attackType.equals("missile")) {
                                if (defenderHigherStructure) {
                                    realScatterDamage += Math.max(netDamage * attackerTechs.getMissileHigherStructureScattering(), 0);
                                } else {
                                    realScatterDamage += Math.max(realScatterDamage * attackerTechs.getMissileLowerStructureScattering(), 0);
                                }
                            }
                            break;
                        case DISTRIBUTED:
                            realScatterDamage = netDamage;
                            break;
                        default:
                            if (commanderOverallScatteringBonus > 0) {
                                realScatterDamage = Math.floor(netDamage * commanderOverallScatteringBonus);
                            } else if (horizontalScatteringRate > 0 && position.getSegmentedPosIndex() == target.getSegmentedPosIndex() &&
                                    MathUtil.isInRange(position.getPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + netDamage * horizontalScatteringRate;
                            } else if (verticalScatteringRate > 0 && position.getPosIndex() == target.getPosIndex() &&
                                    MathUtil.isInRange(position.getSegmentedPosIndex(), 0, 2)) {
                                realScatterDamage = baseScatteringDamage + netDamage * verticalScatteringRate;
                            }
                            break;
                    }
                    if (realScatterDamage > 0) {
                        realScatterDamage = Math.max(realScatterDamage - (realScatterDamage * scatteringReduction), 0);
                        scatteringData.addHit(new ScatteringHit(position, realScatterDamage));
                    }
                }

            }

            // ? Stop if there is no more shields and armor
            if (trueArmorDamage >= defenderArmor || defenderArmor <= 0) {
                break;
            }
            lastTrueArmorDamage = trueArmorDamage;
            lastTrueShieldDamage = trueShieldDamage;

        }

        if (rearReduceStability) {
            defenderStats.setStabilityBonus(defenderStats.getStabilityBonus() - 0.5);
        }
        if (!attackMeta.isAttack()) {
            return this;
        }

        if (trueShieldDamage > 0) {
            // ? Apply tech reflection
            double reflectionRatio = defenderTechs.getBeforeShieldDamageReflectionRateBonus();
            // ? Apply reflection
            if (reflectionRatio > 0) {
                trueReflectionDamage += trueShieldDamage * reflectionRatio;
            }
        }

        if (trueArmorDamage > 0) {
            // ? Reflection variable
            double reflectionRatio = 0.0d;
            // RP Facility #1
            if (optionalReflectingFacility.isPresent()) {
                ShipFacility rpFacility = optionalReflectingFacility.get();
                if (rpFacility.isReloaded(matchRunnable.getRound() + 1)) {
                    int rpReload = (int) defenderTechs.getEnableReflectModulesReactivationAfterRounds();
                    if (rpReload < 0) {
                        rpReload = 1000;
                    }
                    rpFacility.trigger(matchRunnable.getRound() + rpReload);
                    rpFacility.addUsage(defenderUsage);
                }

                if (rpFacility.isEnabled()) {
                    BattleFleetDefensiveModule rpReference = rpFacility.reference();
                    PartLevelMeta levelMeta = rpReference.getMeta();
                    double rpReflection = (Double) levelMeta.getEffect("reflectRatioDamage").getValue();
                    reflectionRatio += rpReflection;
                }
            }
            // ? Apply reflection
            if (reflectionRatio > 0) {
                trueReflectionDamage += trueArmorDamage * reflectionRatio;
            }

        }

        // Daedalus Facility #3
        if (trueArmorDamage > 0 && optionalDaedalusFacility.isPresent()) {
            ShipFacility daedalusFacility = optionalDaedalusFacility.get();
            double facilityStabilityBonus = daedalusFacility.getEffect("stability");
            defenderStats.addStabilityBonus(facilityStabilityBonus);
            double facilityDoubleChance = defenderTechs.getActivatedDaedalusShipStabilityAugmentChance();
            if (MathUtil.randomInclusive(1, 100) <= (facilityDoubleChance * 100.0d)) {
                defenderStats.addStabilityBonus(defenderTechs.getActivatedDaedalusShipStabilityAugment());
            }
            daedalusFacility.addUsage(defenderUsage);
        }

        // Icarus Facility #3
        if (trueArmorDamage > 0 && optionalICSFacility.isPresent()) {
            ShipFacility icarusFacility = optionalICSFacility.get();
            double facilityStabilityBonus = icarusFacility.getEffect("stability");
            defenderStats.addStabilityBonus(facilityStabilityBonus);
            icarusFacility.addUsage(defenderUsage);
        }

        // Add tech stability bonus
        defenderStats.addStabilityBonus(defenderTechs.getShipStabilityBonus());

        // ? Fleet HE3 adjustments before
        // ? the reductions
        if (attackerHe3 > attackerFleet.getHe3()) {
            attackerHe3 = attackerFleet.getHe3();
        }

        if (defenderHe3 > defenderFleet.getHe3()) {
            defenderHe3 = defenderFleet.getHe3();
        }

        // ! Jason [Skill]
        if (!attackerSilent && attackerFleet.isCommanded("commander:jason")) {
            if (attackerFleet.trigger("commander:jason")) {
                attackerHe3 = attackerFleet.getHe3();
                attackMeta.sourceSkill = 1;
            }
        }

        if (!defenderSilent && defenderFleet.isCommanded("commander:jason")) {
            if (defenderFleet.trigger("commander:jason")) {
                defenderHe3 = defenderFleet.getHe3();
                attackMeta.targetSkill = 1;
            }
        }

        double trueAttackerSupply = attackerFleet.getHe3() - attackerHe3;
        double trueDefenderSupply = defenderFleet.getHe3() - defenderHe3;

        // Apply HE3 adjustments
        if (additionalAttackerHe3Cost > 0) {
            trueAttackerSupply *= additionalAttackerHe3Cost;
        }
        if (additionalDefenderHe3Cost > 0) {
            trueDefenderSupply *= additionalDefenderHe3Cost;
        }

        attackMeta.setFromId(attackMeta.getAttackerPos());
        attackMeta.setToId(attackMeta.getDefenderPos());

        attackMeta.setFromShipTeamId(attackerFleet.getShipTeamId());
        attackMeta.setToShipTeamId(defenderFleet.getShipTeamId());

        attackMeta.setFromUserGuid(attackerFleet.getGuid());
        attackMeta.setToUserGuid(defenderFleet.getGuid());

        attackMeta.setFromAmount(attackerCell.getAmount());
        attackMeta.setToAmount(target.getBattleFleetCell().getAmount());

        trigger.setMillis(trigger.getMillis() + 1800);
        trigger.getAttacks().add(attackMeta);

        // * Report the attack
        if ((trueShieldDamage > 0 || trueArmorDamage > 0) || (!scatteringData.getHits().isEmpty())) {

            double highestAttack = trueShieldDamage + trueArmorDamage;

            Optional<ScatteringHit> scatteringGeneralHit = scatteringData.getHits().stream().findFirst();
            if (scatteringGeneralHit.isPresent()) {
                highestAttack += Math.max(scatteringGeneralHit.get().getGeneralDamage(), 0.0d);
            }

            if (highestAttack > 0) {

                ShipHighestAttack attackerHighestAttack = new ShipHighestAttack();

                attackerHighestAttack.setAttacker(attackerFleet.getGuid());
                attackerHighestAttack.setAttackerCell(attackerCell.getShipModelId());
                attackerHighestAttack.setHighestAttack((int) highestAttack);

                attackMeta.setAttackerHighestAttack(attackerHighestAttack);

            }

        }


        // * On-Hit Skills
        if (trueShieldDamage > 0 || trueArmorDamage > 0) {

            // ! Hekatian Witnesses [Skill]
            if (!attackerSilent && attackerFleet.isCommanded("commander:hekatianWitnesses")) {
                if (attackerFleet.trigger("commander:hekatianWitnesses")) {
                    trueShieldDamage = defenderCell.getShields();
                    trueArmorDamage = defenderCell.getStructure();
                    attackMeta.sourceSkill = 1;
                }
            }

            // ! Nora [Skill]
            if (!attackerSilent && attackerFleet.isCommanded("commander:nora")) {
                if (attackerFleet.trigger("commander:nora")) {
                    if (!attackerCommanderAnnahilation) {
                        attackerCommanderAnnahilation = true;
                        trueShieldDamage = defenderCell.getShields();
                        trueArmorDamage = defenderCell.getStructure();
                        attackMeta.sourceSkill = 1;
                    }
                }
            }

            // ! Ringel [Skill] #1
            if (!attackerSilent && attackerFleet.isCommanded("commander:ringel")) {
                if (attackerFleet.trigger("commander:ringel")) {
                    double netAddition = Math.max((trueShieldDamage + trueArmorDamage) * 0.3d, 0.0d);
                    attackMeta.setDefenderEffect(target, EffectType.RINGEL, netAddition, round);
                    attackMeta.sourceSkill = 1;
                }
            }

            // ! Cassius [Skill] #1
            if (!attackerSilent && attackerFleet.isCommanded("commander:cassius")) {
                if (attackerFleet.trigger("commander:cassius")) {
                    if (!attackerCommanderRepelSteps) {
                        attackerCommanderRepelSteps = true;
                        trigger.setRepelSteps(trigger.getRepelSteps() + 3);
                    }
                    attackMeta.sourceSkill = 1;
                }
            }

            // ! Penni [Skill] #2
            if (commanderReflectBonus > 0) {
                trueReflectionDamage += Math.max((trueShieldDamage + trueArmorDamage) * commanderReflectBonus, 0);
            }

            // ! Vinna [Skill] #2
            if (!attackerSilent && attackerFleet.isCommanded("commander:vinna")) {
                if (attackerFleet.trigger("commander:vinna")) {
                    trigger.addEffect(attackerFleet, EffectType.VINNA, 1, round);
                    attackMeta.sourceSkill = 1;
                }
            }

            // ! Joseph [Skill] #2
            if (commanderReflectDamageBonus > 0.0d) {
                trueReflectionDamage += Math.max((trueShieldDamage + trueArmorDamage) * commanderReflectDamageBonus, 0);
            }

        }

        // Reflect flagship
        if (defenderOverallReflectRate > 0) {
            trueReflectionDamage += Math.max((trueShieldDamage + trueArmorDamage) * defenderOverallReflectRate, 0);
        }

        // * Report the reflection
        if (trueReflectionDamage > 0 && !attackerIgnoreReflect) {

            double highestAttack = trueReflectionDamage;
            if (highestAttack > 0) {

                ShipHighestAttack defenderHighestAttack = new ShipHighestAttack();

                defenderHighestAttack.setAttacker(defenderFleet.getGuid());
                defenderHighestAttack.setAttackerCell(defenderCell.getShipModelId());
                defenderHighestAttack.setHighestAttack((int) highestAttack);

                attackMeta.setDefenderHighestAttack(defenderHighestAttack);

            }

        }

        if (attackerIgnoreReflect) {
            trueReflectionDamage = 0;
        }

        // * Define other variables
        ShipReduction reflectionReduction = attackerCell.reflection(trueReflectionDamage, trueAttackerSupply, attackerFleet);

        // * 0 = Attacker normal hit
        // * 1 = Attacker double hits
        // * 4 = Attacker critical attack
        // * 5 = Attacker critical attack - double hits
        if (!stackCritical && !stackDouble) {
            attackMeta.targetBlast = 0;
        } else if (!stackCritical && stackDouble) {
            attackMeta.targetBlast = 1;
        } else if (stackCritical && !stackDouble) {
            attackMeta.targetBlast = 4;
        } else if (stackCritical && stackDouble) {
            attackMeta.targetBlast = 5;
        }

        attackMeta.sourceReduceHp = (int) trueReflectionDamage;
        attackMeta.sourceReduceShipNum = (int) reflectionReduction.getAmountReduction();
        attackMeta.setReflectionReduction(reflectionReduction);

        attackerUsage.map(attackMeta, false);
        defenderUsage.map(attackMeta, true);

        for (ModuleUsage usage : attackerUsage.getPlayback()) {
            attackMeta.getAttackerUsages().add(usage);
        }

        for (ModuleUsage usage : defenderUsage.getPlayback()) {
            attackMeta.getDefenderUsages().add(usage);
        }

        ShipReduction shipReduction = defenderCell.makeReduction(trueShieldDamage, trueArmorDamage, trueDefenderSupply, commanderMaximumStability, defenderStats, defenderFleet);
        shipReduction.setPosition(target);

        // * Report reductions
        if (reflectionReduction.getAmountReduction() > 0) {

            ShipShootdowns shipShootdowns = new ShipShootdowns();

            shipShootdowns.setAttacker(defenderFleet);
            shipShootdowns.setAttackerCell(defenderCell);
            shipShootdowns.setAmount((int) reflectionReduction.getAmountReduction());

            attackMeta.getShipShootdowns().add(shipShootdowns);

        }
        if (shipReduction.getAmountReduction() > 0) {

            ShipShootdowns shipShootdowns = new ShipShootdowns();

            shipShootdowns.setAttacker(attackerFleet);
            shipShootdowns.setAttackerCell(attackerCell);
            shipShootdowns.setAmount((int) shipReduction.getAmountReduction());

            attackMeta.getShipShootdowns().add(shipShootdowns);

            // ! Rocky [Skill] #2 & The Ravagers [Skill] #2
            if (attackerFleet.hasRocky() && !attackerSilent && !attackerFleet.isNullified()) {
                if (!defenderCell.hasShips()) {
                    if (attackerFleet.isCommanded("commander:theRavagers")) {
                        //instant recharge all weapons
                        damages.forEach((x) -> {
                            x.shoot(-10000);
                        });
                    }
                    if (!attackerFleet.getEffects().contains(EffectType.ROCKY)) {
                        trigger.addEffect(attackerFleet, EffectType.ROCKY, 1, matchRunnable.getMatch().getRound() + 1);
                        attackMeta.sourceSkill = 1;
                    } else {
                        trigger.addEffect(attackerFleet, EffectType.ROCKY, 1, matchRunnable.getMatch().getRound() + 1);
                    }
                }
            }

        }

        // * Scattering reductions
        for (ScatteringHit scatteringHit : scatteringData.getHits()) {

            ShipPosition position = scatteringHit.getPosition();
            if (!position.getBattleFleetCell().hasShips()) {
                continue;
            }

            ShipReduction scatteringReduction = position.getBattleFleetCell().reflection(scatteringHit.getGeneralDamage(), 0.0d, defenderFleet);
            scatteringReduction.setPosition(position);

            attackMeta.getShipReductions().add(scatteringReduction);

            attackMeta.targetReduceShield[position.getPos()] = (int) scatteringReduction.getReflectionReduction();
            attackMeta.targetReduceShipNum[position.getPos()] = (int) scatteringReduction.getAmountReduction();

            attackMeta.targetReduceSupply += (int) scatteringReduction.getSupplyReduction();
            attackMeta.targetReduceStorage += (int) scatteringReduction.getStorageReduction();

            if (scatteringReduction.getAmountReduction() > 0) {

                ShipShootdowns shipShootdowns = new ShipShootdowns();

                shipShootdowns.setAttacker(attackerFleet);
                shipShootdowns.setAttackerCell(attackerCell);
                shipShootdowns.setAmount((int) scatteringReduction.getAmountReduction());

                attackMeta.getShipShootdowns().add(shipShootdowns);
            }

        }

        attackMeta.getShipReductions().add(shipReduction);

        attackMeta.sourceReduceSupply = (int) reflectionReduction.getSupplyReduction();
        attackMeta.sourceReduceStorage = (int) reflectionReduction.getStorageReduction();

        attackMeta.targetReduceShield[attackMeta.getDefenderPos()] = (int) shipReduction.getShieldsReduction();
        attackMeta.targetReduceStructure[0] = (int) shipReduction.getStructureReduction();

        attackMeta.targetReduceShipNum[attackMeta.getDefenderPos()] = (int) shipReduction.getAmountReduction();

        attackMeta.targetReduceSupply += (int) shipReduction.getSupplyReduction();
        attackMeta.targetReduceStorage += (int) shipReduction.getStorageReduction();

        return this;

    }

    public ShipAttackCalculator calculate(ShipPosition attacker, AssaultCellAttackMeta attackMeta, FleetAttackFortTrigger trigger) {

        if (!(defenderElement instanceof BattleFort defenderFort)) {
            return this;
        }

        if (defenderFort.isDestroyed()) {
            return this;
        }

        BattleFleetCell attackerCell = attacker.getBattleFleetCell();

        Node attackerNode = attackerFleet.getNode();
        Node defenderNode = defenderFort.getNode();

        int distance = attackerNode.getHeuristic(defenderNode);

        // Whole attack modules
        ShipAttack shipAttack = attackerCell.getWeaponsToAttack(matchRunnable.getMatch().getRound(), distance, attackerFleet.getTechs(), true);
        if (shipAttack.getAttackModules().isEmpty()) {
            return this;
        }

        List<BattleFleetAttackModule> attackModules = shipAttack.getAttackModules();

        BattleCommander attackCommander = attackerFleet.getBattleCommander();
        int attackerEffectiveStack = attackCommander.getEffectiveStack(attackerCell);
        ShipUsage attackerUsage = new ShipUsage();

        //
        // ? Based on: https://galaxyonlineii.fandom.com/wiki/Combat_Mechanics#hit_chance_formula
        // ? Next we will follow all of these steps for make the combat calculation
        //

        // * [Step -1. Calculate additional effects] - Passive modules
        // * body type passives.
        ShipStats attackerStats = new ShipStats();

        // ? Ship models effects aggregation
        attackerStats.pass(attackerFleet);
        attackerStats.pass(attackerCell.getModel());

        // * [Step 0. Total damage mitigation] (All types) - Calculate all damage
        // * that can be negated by the shields and structure and save in a
        // * data structure for be used to know how many modules should use.

        // * [Step 0.1. Calculate current interceptions] - Calculate the interceptions
        // * that will be tried (determined by a random number generator)

        // * [Step 0.2. Calculate each module stack] - Calculate the
        // * values for each attack module stack.
        List<ShipDamage> damages = new ArrayList<>();

        LinkedHashMap<Integer, List<BattleFleetAttackModule>> mappedAttacks = attackModules.stream()
                .collect(Collectors.groupingBy(module -> module.getModuleId(), LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<Integer, List<BattleFleetAttackModule>> attackModule : mappedAttacks.entrySet()) {

            ShipDamage shipDamage = new ShipDamage();

            for (BattleFleetAttackModule module : attackModule.getValue()) {
                shipDamage.add(module);
            }

            shipDamage.calculate(attackerEffectiveStack);
            damages.add(shipDamage);

        }

        // * [Step 0.3. List necessary attack modules] - List all attack modules
        // * needed for destroy the whole defenderStack (this will take into account
        // * negation and interception).

        double virtualEndure = defenderFort.getHealth();

        double trueEndureDamage = 0;
        double attackerHe3 = attackerFleet.getHe3();

        ShipTechs attackerTechs = attackerFleet.getTechs();

        Iterator<ShipDamage> damageIterator = damages.listIterator();

        while ((virtualEndure > trueEndureDamage) && damageIterator.hasNext()) {
            double virtualDamage;

            ShipDamage shipDamage = damageIterator.next();
            BattleFleetAttackModule reference = shipDamage.getReference();
            String attackType = shipDamage.getAttackType();
            String damageType = shipDamage.getDamageType();
            double additionalMinAttack = attackCommander.getAdditionalMinAttack(attackType);
            double additionalMaxAttack = attackCommander.getAdditionalMaxAttack(attackType);
            if (attackerHe3 < shipDamage.getFuelUsage(attackerEffectiveStack)) {
                continue;
            }

            // ? Set attack basics
            attackMeta.setAttack(true);
            attackerHe3 = Math.max(attackerHe3 - (shipDamage.getFuelUsage(attackerEffectiveStack)), 0);

            // ? Weapon reload
            shipDamage.shoot(matchRunnable.getMatch().getRound() + 1);


            // ? Calculate hit rate
            // ? Agility decrease hit chance (1 agility decreases 4%)
            // ? Steering increase hit chance (1 steering increases 4%)
            // ? Each 12 dodge reduce hit chance by 1%
            // ? Each 12 accuracy increase hit chance by 1%
            double steering = attackerStats.getSteering() + shipDamage.getSteering();
            double hitRate = shipDamage.getReference().getHitRate() + (1 + steering - 0) * 0.04;
            if (hitRate > 1) {
                hitRate = 1;
            } else if (hitRate < 0) {
                hitRate = 0;
            }

            // ? Intercept if is possible
            double originalHits = shipDamage.getHits();
            double shipHits = originalHits * hitRate;

            // ? Calculate damages
            virtualDamage = RandomUtil.getSmartRandomInt((int) (reference.getMinAttack() + additionalMinAttack), (int) (reference.getMaxAttack() + additionalMaxAttack)) * shipHits;

            // ? Planet defenses are always
            // ? Light Armor (90% reduction)
            double againstLightDamageBonus = 0.0;
            double reduction = 0.9;
            if (attackType.equals("ballistic")) {
                if (damageType.equals("kinetic")) {
                    reduction -= attackerTechs.getBallisticKineticDamageAgainstLightRate();
                } else if (damageType.equals("heat")) {
                    reduction -= attackerTechs.getBallisticHeatDamageAgainstLightRate();
                }
                reduction -= attackerTechs.getBallisticDamageAgainstLightRate();
            }
            if (attackerStats.getArmorBonus().containsKey("light")) {
                againstLightDamageBonus += attackerStats.getArmorBonus().get("light");
            }

            // ! Miller [Skill]
            if (attackerFleet.isCommanded("commander:miller")) {
                againstLightDamageBonus += 3.0d;
            }

            reduction -= againstLightDamageBonus;

            // ? Check damage bonuses
            // ? based on formation
            virtualDamage = switch (attackMeta.getAttackerSegmentedPosIndex()) {
                case 1 -> // Segment 2
                        Math.max(virtualDamage - (virtualDamage * 0.10), 0);
                case 2 -> // Segment 3
                        Math.max(virtualDamage - (virtualDamage * 0.25), 0);
                default -> virtualDamage;
            };

            // ? Calculate reduction
            virtualDamage = Math.max(virtualDamage - (virtualDamage * reduction), 0);

            // ? Apply shield damage with reductions
            trueEndureDamage += Math.min(virtualDamage, virtualEndure);

            // ? Add attack usage
            shipDamage.addUsage(attackerUsage, hitRate);

            // ? Stop if there is no more shields and armor
            if (trueEndureDamage >= virtualEndure) {
                break;
            }

        }

        if (!attackMeta.isAttack()) {
            return this;
        }

        // ? Fleet HE3 adjustments before
        // ? the reductions
        if (attackerHe3 > attackerFleet.getHe3()) {
            attackerHe3 = attackerFleet.getHe3();
        }

        attackMeta.setFromId(attackMeta.getAttackerPos());
        attackMeta.setToId(defenderFort.getFortId());

        attackMeta.setFromShipTeamId(attackerFleet.getShipTeamId());
        attackMeta.setToFortId(defenderFort.getFortId());

        attackMeta.setFromUserGuid(attackerFleet.getGuid());
        attackMeta.setToUserGuid(0);

        attackMeta.setFromAmount(attackerCell.getAmount());

        trigger.getAttacks().add(attackMeta);

        attackMeta.sourceReduceHp = 0;
        attackerUsage.map(attackMeta);

        for (ModuleUsage usage : attackerUsage.getPlayback()) {
            attackMeta.getModuleUsages().add(usage);
        }

        trueEndureDamage = Math.max(trueEndureDamage, 0);

        if (defenderFort.getFortType().isStation() && trueEndureDamage > 0) {
            trueEndureDamage = 1;
        }

        FortReduction fortReduction = defenderFort.makeReduction(trueEndureDamage);
        attackMeta.getFortReductions().add(fortReduction);

        if (defenderFort.isDestroyed()) {
            ShipShootdowns shipShootdowns = new ShipShootdowns();
            shipShootdowns.setAttacker(attackerFleet);
            shipShootdowns.setAttackerCell(attackerCell);
            shipShootdowns.setAmount(1);
            attackMeta.getShipShootdowns().add(shipShootdowns);
        }

        attackMeta.sourceReduceSupply = 0;
        attackMeta.sourceReduceStorage = 0;

        attackMeta.targetReduceHealth = (int) fortReduction.getHealthReduction();
        return this;

    }

}

package com.go2super.service.battle;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.database.entity.sub.battle.meta.AssaultCellAttackMeta;
import com.go2super.database.entity.sub.battle.meta.FortCellAttackMeta;
import com.go2super.database.entity.sub.battle.meta.ShipCellAttackMeta;
import com.go2super.database.entity.sub.battle.trigger.*;
import com.go2super.database.entity.type.BattleElementType;
import com.go2super.database.entity.type.MatchType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.CharArray;
import com.go2super.obj.game.FortressFight;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.ShipFight;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import com.go2super.packet.fight.ResponseFightFortressSectionPacket;
import com.go2super.packet.fight.ResponseFightInitShipTeamPacket;
import com.go2super.packet.fight.ResponseFightSectionPacket;
import com.go2super.packet.ship.ResponseGalaxyShipPacket;
import com.go2super.service.BattleService;
import com.go2super.service.CommanderService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.calculator.*;
import com.go2super.service.battle.comparator.IntervalComparator;
import com.go2super.service.battle.match.IglMatch;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import com.go2super.service.battle.pathfinder.GO2Node;
import com.go2super.service.battle.pathfinder.GO2Path;
import com.go2super.service.battle.type.*;
import com.go2super.socket.util.MathUtil;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Data
public class MatchRunnable implements Runnable {

    public static final int[][][] segmentedMatrix = {
            new int[][]{
                    {6, 7, 8},
                    {3, 4, 5}, // 0
                    {0, 1, 2}
            },
            new int[][]{
                    {0, 3, 6},
                    {1, 4, 7}, // 1
                    {2, 5, 8}
            },
            new int[][]{
                    {0, 1, 2},
                    {3, 4, 5}, // 2
                    {6, 7, 8}
            },
            new int[][]{
                    {8, 5, 2},
                    {7, 4, 1}, // 3
                    {6, 3, 0}
            }
    };

    private final Match match;
    private final AtomicBoolean interrupted = new AtomicBoolean(false);

    private long actionFinishDate;

    public MatchRunnable(Match match) {

        this.match = match;

    }

    @Override
    @SneakyThrows
    public void run() {

        try {

            while (!getInterrupted().get()) {
                if (!match.isWatchMode()) {
                    Thread.sleep(55L);
                    if (match.getPause().get()) {
                        continue;
                    }
                }
                frame();
            }

        } catch (Exception e) {

            BotLogger.error("(MATCH) [E] Match exception:");
            BotLogger.error(e);

            match.stop(StopCause.MANUAL);

        }

    }

    public void frame() {

        // match.print();

        if (match.getRound() == -1) {

            // Initialize the report
            match.getBattleReport().start(match);

            // Start
            start();
            return;

        }

        // One match step
        step();

    }

    public void start() {
        Match match = getMatch();
        BotLogger.info("Starting match (Id: " + match.getId() + ", MaxRound: " + match.getMaxRound() + ", Type: " + match.getMatchType() + ")");

        if (match.getRound() == -1 || match.getBattleAction() == null) {
            calculateNextRound();
            if (getMatch().isWatchMode()) {
                List<BattleFleet> battleFleetList = match.getFleetsSorted();
                ResponseGalaxyShipPacket response = null;
                for (BattleFleet battleFleet : battleFleetList) {
                    if (response == null) {
                        response = new ResponseGalaxyShipPacket();
                        response.setGalaxyId(match.getGalaxyId());
                        response.setGalaxyMapId((short) 0);
                        response.setFleets(new ArrayList<>());
                    } else if (response.getFleets().size() == 189) {
                        match.getPackets().add(response);
                        response = new ResponseGalaxyShipPacket();
                        response.setGalaxyId(match.getGalaxyId());
                        response.setGalaxyMapId((short) 0);
                        response.setFleets(new ArrayList<>());
                    }
                    IglMatch iglMatch = (IglMatch) match;
                    User user = UserService.getInstance().getUserCache().findByGuid(iglMatch.getSourceGuid());
                    GalaxyFleetInfo fleetInfo = GalaxyFleetInfo.builder()
                            .shipTeamId(battleFleet.getShipTeamId())
                            .shipNum(battleFleet.ships())
                            .bodyId((short) battleFleet.getBodyId())
                            .reserve((short) 1)
                            .direction((byte) battleFleet.getDirection())
                            .posX((byte) battleFleet.getPosX())
                            .posY((byte) battleFleet.getPosY())
                            .owner((byte) (BattleService.getInstance().getFleetColor(user, battleFleet)))
                            .build();
                    response.getFleets().add(fleetInfo);
                    response.setDataLen((short) response.getFleets().size());
                    match.getPackets().add(response);
                }
            }
        }

    }

    public void step() {

        if (new Date().getTime() <= actionFinishDate && !match.isWatchMode()) {
            return;
        }

        if (!match.canContinue()) {
            if (match.getMatchType() == MatchType.PVP_MATCH) {
                boolean hasEnemiesInNextRound = PacketService.getInstance().getFleetCache().findAllByGalaxyIdAndMatch(getMatch().getGalaxyId(), false)
                        .stream()
                        .anyMatch(fleet -> fleet.getFleetTransmission() == null && fleet.getFleetInitiator() != null && fleet.getFleetInitiator().getJumpType() == JumpType.ATTACK);
                if (hasEnemiesInNextRound) {
                    calculateNextRound();
                    return;
                }
            }

            // Finish match
            interrupted.set(true);
            // BotLogger.log("STOP!");
            stop();
            return;

        }


        if (match.getAction() == match.getCurrentRound().getActions().size()) {

            int nextRound = match.getRound() + 1;

            if (nextRound > match.getMaxRound() || nextRound > 100) {

                // Finish match
                interrupted.set(true);
                stop();
                return;

            }

            calculateNextRound();
            return;

        }

        // Current battle action
        BattleAction current = match.getCurrentRound().getActions().get(match.getAction());
        calculateNextFightPacket(current);

        match.setAction(match.getAction() + 1);
        // match.printCurrent(); // Print current

    }

    //
    // todo
    // If is a war match type
    // all the real fleets need to
    // suffered real changes.
    //
    public void stop() {

        BotLogger.info("Stopping match (Id: " + getMatch().getId() + ", CurrentRound: " + getMatch().getRound() + ", MaxRound: " + getMatch().getMaxRound() + ", Type: " + getMatch().getMatchType() + ")");
        BattleService.getInstance().stopMatch(match, StopCause.AUTOMATIC);

    }

    public void calculateNextFightPacket(BattleAction battleAction) {

        actionFinishDate = new Date().getTime();

        // Set ship team based fight section
        if (battleAction.getType().equals("action:ship.team.procedure")) {

            ResponseFightSectionPacket fightSectionPacket = BattleService.getInstance().getEmptyFightSection(match);

            fightSectionPacket.setSourceShipTeamId(battleAction.getInvolvedId());
            fightSectionPacket.setToShipTeamId(-1);

            calculateNextFightSection(fightSectionPacket, battleAction);

        } else if (battleAction.getType().equals("action:fort.procedure")) {

            calculateNextFightFortressSection(battleAction);

        }

    }

    public void calculateNextFightFortressSection(BattleAction battleAction) {

        List<Packet> fightFortressPackets = new ArrayList<>();

        ResponseFightFortressSectionPacket fightFortressSectionPacket = null;
        BattleFort battleFort = getCurrentFort(battleAction.getInvolvedId());

        for (BattleActionTrigger trigger : battleAction.getTriggers()) {

            // Add millis for calculate good needTime
            actionFinishDate += trigger.getMillis();

            // Switch between triggers
            if (trigger instanceof FortAttackFleetTrigger attackTrigger) {
                battleFort.setNextAttack(attackTrigger.getNextAttack());

                if (attackTrigger.getShipAttackPacketActions() != null) {
                    for (ShipAttackPacketAction packetAction : attackTrigger.getShipAttackPacketActions()) {
                        packetAction.execute();
                    }
                }

                Set<Integer> targetFleets = new HashSet<>();

                for (FortCellAttackMeta attackMeta : attackTrigger.getAttacks()) {

                    if (attackMeta == null || !attackMeta.isAttack()) {
                        continue;
                    }

                    if (fightFortressSectionPacket == null) {

                        fightFortressSectionPacket = BattleService.getInstance().getEmptyFightFortressSection(match);

                        fightFortressSectionPacket.setBuildType((byte) battleFort.getBuildType());
                        fightFortressSectionPacket.setSourceId(attackTrigger.getAttackerFortId());

                    } else if (fightFortressSectionPacket.getFortressFights().size() == 25) {

                        fightFortressPackets.add(fightFortressSectionPacket);

                        fightFortressSectionPacket = BattleService.getInstance().getEmptyFightFortressSection(match);

                        fightFortressSectionPacket.setBuildType((byte) battleFort.getBuildType());
                        fightFortressSectionPacket.setSourceId(attackTrigger.getAttackerFortId());

                    }

                    // Current defender fleet
                    BattleFleet targetBattleFleet = getCurrentFleet(attackMeta.getTargetShipTeamId());
                    targetFleets.add(attackMeta.getTargetShipTeamId());

                    FortressFight fortressFight = new FortressFight();

                    fortressFight.setTargetShipTeamId(attackMeta.getTargetShipTeamId());
                    fortressFight.setTargetReduceSupply(attackMeta.getTargetReduceSupply());
                    fortressFight.setTargetReduceStorage(attackMeta.getTargetReduceStorage());
                    fortressFight.setTargetReduceHp(attackMeta.getTargetReduceHp());
                    fortressFight.setTargetReduceShipNum(attackMeta.getTargetReduceShipNum());

                    fightFortressSectionPacket.getFortressFights().add(fortressFight);
                    fightFortressSectionPacket.setDataLen((byte) fightFortressSectionPacket.getFortressFights().size());

                    // BotLogger.log(attackMeta);
                    // todo attack translation to client
                    for (ShipReduction reduction : attackMeta.getShipReductions()) {

                        BattleFleetCell battleFleetCell = targetBattleFleet.getCell(reduction.getPosition().getPos());
                        battleFleetCell.doReduction(reduction, targetBattleFleet);

                    }

                    // Report Shootdowns
                    for (FortShootdowns fortShootdowns : attackMeta.getFortShootdowns()) {

                        match.getBattleReport().processShootdowns(fortShootdowns);

                    }

                    // BattleFleetCell targetCell = targetTeam.getCells().get(attackMeta.getDefenderPos());
                    // targetCell.doReduction(attackMeta.getShipReduction());

                    // Just for debug
                    if (attackMeta.getShipAttackPacketActions() != null) {
                        for (ShipAttackPacketAction packetAction : attackMeta.getShipAttackPacketActions()) {
                            packetAction.execute();
                        }
                    }

                    boolean targetDestroyed = targetBattleFleet.isDestroyed();
                    if (targetDestroyed) {
                        fortressFight.setDelFlag((byte) 1);
                    }
                    targetBattleFleet.update(match);

                }

            }

        }

        if (fightFortressSectionPacket != null) {
            fightFortressPackets.add(fightFortressSectionPacket);
        }
        match.sendPacketToViewers(fightFortressPackets);

    }

    @SneakyThrows
    public void calculateNextFightSection(ResponseFightSectionPacket fightSectionPacket, BattleAction battleAction) {
        BattleFleet battleFleet = getCurrentFleet(battleAction.getInvolvedId());

        for (AreaEffect effect : battleAction.getAreaEffects()) {
            if (effect.getSourceShipTeamId() != battleFleet.getShipTeamId()) {
                continue;
            }
            for (Integer targetId : effect.getTargetShipTeamIds()) {
                BattleFleet currentFleet = getCurrentFleet(targetId);
                if (currentFleet == null) {
                    continue;
                }
                for (BattleFleetCell cell : currentFleet.getCells()) {
                    doAreaEffect(effect.type, cell);
                }
            }
        }

        int[] movementPath = new int[16];
        int movementPathIndex = 0;

        Arrays.fill(movementPath, -1);

        for (BattleActionTrigger trigger : battleAction.getTriggers()) {

            // Add millis for calculate good needTime
            actionFinishDate += trigger.getMillis();
            fightSectionPacket.setMillis(fightSectionPacket.getMillis() + trigger.getMillis());
            // Switch between triggers
            if (trigger instanceof MovementTrigger movementTrigger) {

                int packetMovementId = movementTrigger.calculatePathMovement();
                if (movementPathIndex >= 16) {
                    continue;
                }
                movementPath[movementPathIndex++] = packetMovementId;

                battleFleet.setPosX(movementTrigger.getToX());
                battleFleet.setPosY(movementTrigger.getToY());

                fightSectionPacket.setSourceDirection((byte) packetMovementId);
                fightSectionPacket.setSourceSkill(UnsignedChar.of(movementTrigger.isSourceAnimation() ? 1 : 0));
                battleFleet.setDirection(packetMovementId);
                battleFleet.update(match);

            }

            if (trigger instanceof FleetRotateTrigger rotateTrigger) {

                fightSectionPacket.setSourceDirection((byte) rotateTrigger.getDirection());

                battleFleet.setDirection(rotateTrigger.getDirection());
                battleFleet.update(match);

            }

            if (trigger instanceof FleetAttackFleetTrigger attackTrigger) {

                fightSectionPacket.setShipFights(new ArrayList<>());

                fightSectionPacket.setToShipTeamId(attackTrigger.getDefenderShipTeamId());

                if (attackTrigger.getShipAttackPacketActions() != null) {
                    for (ShipAttackPacketAction packetAction : attackTrigger.getShipAttackPacketActions()) {
                        packetAction.execute();
                    }
                }

                // Apply Fleet Effects
                for (FleetEffects fleetEffects : attackTrigger.getFleetEffects()) {

                    BattleFleet targetFleet = getCurrentFleet(fleetEffects.getShipTeamId());
                    if (targetFleet.isDestroyed()) {
                        continue;
                    }

                    for (FleetEffect fleetEffect : fleetEffects.getEffects()) {
                        targetFleet.process(fleetEffect);
                    }

                }

                BattleFleet targetBattleFleet = getCurrentFleet(attackTrigger.getDefenderShipTeamId());
                List<ResponseFightInitShipTeamPacket> shipTeamPackets = BattleService.getInstance().getFightInitShipTeamPackets(match, battleFleet, targetBattleFleet, attackTrigger.getDefensiveDirection());

                match.sendPacketToViewers(shipTeamPackets.toArray(ResponseFightInitShipTeamPacket[]::new));

                // Apply Repel
                int repel = attackTrigger.getRepelSteps();

                if (attackTrigger.getRepelSteps() > 0) {

                    switch (attackTrigger.getAttackerDirection()) {
                        case 0:
                            targetBattleFleet.setPosX(Math.max(0, Math.min(targetBattleFleet.getPosX() + repel, 24)));
                            break;
                        case 1:
                            targetBattleFleet.setPosY(Math.max(0, Math.min(targetBattleFleet.getPosY() + repel, 24)));
                            break;
                        case 2:
                            targetBattleFleet.setPosX(Math.max(0, Math.min(targetBattleFleet.getPosX() - repel, 24)));
                            break;
                        default:
                            targetBattleFleet.setPosY(Math.max(0, Math.min(targetBattleFleet.getPosY() - repel, 24)));
                            break;
                    }

                    fightSectionPacket.setRepelStep((byte) repel);

                }

                int[] array = {-1, -1, -1, -1, -1, -1, -1, -1, -1};

                for (int i = 0; i < 9; i++) {

                    ShipCellAttackMeta attackMeta = attackTrigger.getMeta(i);

                    if (attackMeta == null || !attackMeta.isAttack()) {

                        fightSectionPacket.getShipFights().add(new ShipFight());
                        continue;

                    }

                    // Current cells
                    BattleFleetCell attackerCell = battleFleet.getCell(attackMeta.getAttackerPos());
                    BattleFleetCell defenderCell = targetBattleFleet.getCell(attackMeta.getDefenderPos());

                    // Apply attacker effects
                    for (Map.Entry<ShipPosition, ShipEffects> entry : attackMeta.getAttackerEffects().entrySet()) {

                        ShipPosition attackerPos = entry.getKey();
                        ShipEffects attackerEffects = entry.getValue();

                        for (ShipEffect attackerEffect : attackerEffects.getEffects()) {

                            BattleFleetCell affectedCell = battleFleet.getCell(attackerPos.getPos());
                            affectedCell.process(attackerEffect);

                        }

                    }

                    // Apply defender effects
                    for (Map.Entry<ShipPosition, ShipEffects> entry : attackMeta.getDefenderEffects().entrySet()) {

                        ShipPosition defenderPos = entry.getKey();
                        ShipEffects defenderEffects = entry.getValue();

                        for (ShipEffect defenderEffect : defenderEffects.getEffects()) {

                            BattleFleetCell affectedCell = targetBattleFleet.getCell(defenderPos.getPos());
                            affectedCell.process(defenderEffect);

                        }

                    }

                    array[i] = attackMeta.getDefenderPos(); // 0 means that the client needs to animate attack from one ship to the enemy fleet, -1 means do nothing
                    ShipFight shipFight = new ShipFight();

                    shipFight.setSourceReduceSupply(attackMeta.getSourceReduceSupply());
                    shipFight.setTargetReduceSupply(attackMeta.getTargetReduceSupply());

                    shipFight.setSourceReduceStorage(attackMeta.getSourceReduceStorage());
                    shipFight.setTargetReduceStorage(attackMeta.getTargetReduceStorage());

                    shipFight.setSourceReduceShipNum(attackMeta.getSourceReduceShipNum());
                    shipFight.setSourceReduceHp(attackMeta.getSourceReduceHp());

                    shipFight.setTargetReduceShield(attackMeta.targetReduceShieldBuffer());
                    shipFight.setTargetReduceEndure(attackMeta.getTargetReduceStructure()[0]); // todo check
                    shipFight.setTargetReduceShipNum(attackMeta.targetReduceShipNumBuffer());

                    shipFight.setSourcePartId(attackMeta.sourcePartIdBuffer());
                    shipFight.setSourcePartNum(attackMeta.sourcePartNumBuffer());
                    shipFight.setSourcePartRate(attackMeta.sourcePartRateBuffer());

                    shipFight.setTargetPartId(attackMeta.targetPartIdBuffer());
                    shipFight.setTargetPartNum(attackMeta.targetPartNumBuffer());

                    shipFight.setSourceSkill(attackMeta.getSourceSkill());
                    shipFight.setTargetSkill(attackMeta.getTargetSkill());
                    shipFight.setTargetBlast(attackMeta.getTargetBlast());

                    fightSectionPacket.getShipFights().add(shipFight);

                    // Reflection Damage
                    if (attackMeta.getReflectionReduction() != null) {
                        attackerCell.doReduction(attackMeta.getReflectionReduction(), battleFleet);
                    }

                    // Attacker Module Usages
                    for (ModuleUsage moduleUsage : attackMeta.getAttackerUsages()) {

                        if (attackerCell.isEmpty()) {
                            continue;
                        }

                        BattleFleetAttackModule module = attackerCell.getWeaponByIndex(moduleUsage.getModuleIndex(), false);

                        module.setReload(moduleUsage.getReload());
                        module.setLastShoot(moduleUsage.getLastShoot());

                    }

                    // Defender Module Usages
                    for (ModuleUsage moduleUsage : attackMeta.getDefenderUsages()) {

                        if (defenderCell.isEmpty()) {
                            continue;
                        }
                        BattleFleetDefensiveModule module = defenderCell.getDefenseByIndex(moduleUsage.getModuleIndex());

                        module.setReload(moduleUsage.getReload());
                        module.setLastShoot(moduleUsage.getLastShoot());

                    }

                    // BotLogger.log(attackMeta);
                    // todo attack translation to client
                    for (ShipReduction reduction : attackMeta.getShipReductions()) {

                        BattleFleetCell battleFleetCell = targetBattleFleet.getCell(reduction.getPosition().getPos());
                        battleFleetCell.doReduction(reduction, targetBattleFleet);

                    }

                    // Report Shootdowns
                    for (ShipShootdowns shipShootdowns : attackMeta.getShipShootdowns()) {

                        match.getBattleReport().processShootdowns(shipShootdowns);

                    }

                    // Report The Highest Attack
                    if (attackMeta.getAttackerHighestAttack() != null) {
                        match.getBattleReport().processHighestAttack(attackMeta.getAttackerHighestAttack());
                    }
                    if (attackMeta.getDefenderHighestAttack() != null) {
                        match.getBattleReport().processHighestAttack(attackMeta.getDefenderHighestAttack());
                    }

                    // BattleFleetCell targetCell = targetTeam.getCells().get(attackMeta.getDefenderPos());
                    // targetCell.doReduction(attackMeta.getShipReduction());

                    // Just for debug
                    if (attackMeta.getShipAttackPacketActions() != null) {
                        for (ShipAttackPacketAction packetAction : attackMeta.getShipAttackPacketActions()) {
                            packetAction.execute();
                        }
                    }

                }

                boolean targetDestroyed = targetBattleFleet.isDestroyed();
                boolean sourceDestroyed = battleFleet.isDestroyed();

                int delFlag = targetDestroyed && sourceDestroyed ? 3 : (sourceDestroyed) ? 2 : (targetDestroyed) ? 1 : 0;
                fightSectionPacket.setDelFlag((byte) delFlag);

                if (targetDestroyed) {
                    match.removeFleet(targetBattleFleet);
                }
                if (sourceDestroyed) {
                    match.removeFleet(battleFleet);
                }

                fightSectionPacket.setTargetMatrixId(new CharArray(array));

                battleFleet.update(match);
                targetBattleFleet.update(match);

            }

            if (trigger instanceof FleetAttackFortTrigger attackTrigger) {

                fightSectionPacket.setBothStatus((byte) 1);
                fightSectionPacket.setShipFights(new ArrayList<>());

                fightSectionPacket.setToShipTeamId(attackTrigger.getDefenderFortId());

                if (attackTrigger.getShipAttackPacketActions() != null) {
                    for (ShipAttackPacketAction packetAction : attackTrigger.getShipAttackPacketActions()) {
                        packetAction.execute();
                    }
                }

                BattleFort targetBattleFort = getCurrentFort(attackTrigger.getDefenderFortId());
                List<ResponseFightInitShipTeamPacket> shipTeamPackets = BattleService.getInstance().getFightInitShipTeamPackets(match, battleFleet);

                // battleFleet.setDirection(attackTrigger.getAttackerDirection());
                // targetBattleFleet.setDirection(attackTrigger.getDefensiveDirection());

                match.sendPacketToViewers(shipTeamPackets.stream().toArray(ResponseFightInitShipTeamPacket[]::new));

                int[] array = {-1, -1, -1, -1, -1, -1, -1, -1, -1};

                for (int i = 0; i < 9; i++) {

                    AssaultCellAttackMeta attackMeta = attackTrigger.getMeta(i);

                    if (attackMeta == null || !attackMeta.isAttack()) {

                        fightSectionPacket.getShipFights().add(new ShipFight());
                        continue;

                    }

                    // Current attacker cell
                    BattleFleetCell attackerCell = battleFleet.getCell(attackMeta.getAttackerPos());

                    array[i] = -1; // 0 means that the client needs to animate attack from one ship to the enemy fleet, -1 means do nothing
                    ShipFight shipFight = new ShipFight();

                    shipFight.setSourceReduceSupply(attackMeta.getSourceReduceSupply());
                    shipFight.setTargetReduceSupply(0);

                    shipFight.setSourceReduceStorage(attackMeta.getSourceReduceStorage());
                    shipFight.setTargetReduceStorage(0);

                    shipFight.setSourceReduceShipNum(attackMeta.getSourceReduceShipNum());
                    shipFight.setSourceReduceHp(attackMeta.getSourceReduceHp());
                    shipFight.setTargetReduceEndure(attackMeta.targetReduceHealth); // todo check

                    shipFight.setSourcePartId(attackMeta.sourcePartIdBuffer());
                    shipFight.setSourcePartNum(attackMeta.sourcePartNumBuffer());
                    shipFight.setSourcePartRate(attackMeta.sourcePartRateBuffer());

                    shipFight.setSourceSkill(attackMeta.getSourceSkill());
                    shipFight.setTargetSkill(attackMeta.getTargetSkill());
                    shipFight.setTargetBlast(attackMeta.getTargetBlast());

                    fightSectionPacket.getShipFights().add(shipFight);

                    // Reflection Damage
                    if (attackMeta.getReflectionReduction() != null) {
                        attackerCell.doReduction(attackMeta.getReflectionReduction(), battleFleet);
                    }

                    // Attack Module Usages
                    for (ModuleUsage moduleUsage : attackMeta.getModuleUsages()) {

                        if (attackerCell.isEmpty()) {
                            continue;
                        }

                        BattleFleetAttackModule module = attackerCell.getWeaponByIndex(moduleUsage.getModuleIndex(), true);
                        module.setReload(moduleUsage.getReload() + module.getCooldown());

                    }

                    // BotLogger.log(attackMeta);
                    // todo attack translation to client
                    for (FortReduction reduction : attackMeta.getFortReductions()) {
                        targetBattleFort.doReduction(reduction);
                    }

                    // Report Shootdowns
                    for (ShipShootdowns shipShootdowns : attackMeta.getShipShootdowns()) {

                        match.getBattleReport().processShootdowns(shipShootdowns);

                    }

                    // BattleFleetCell targetCell = targetTeam.getCells().get(attackMeta.getDefenderPos());
                    // targetCell.doReduction(attackMeta.getShipReduction());

                    // Just for debug
                    if (attackMeta.getShipAttackPacketActions() != null) {
                        for (ShipAttackPacketAction packetAction : attackMeta.getShipAttackPacketActions()) {
                            packetAction.execute();
                        }
                    }

                }

                boolean targetDestroyed = targetBattleFort.isDestroyed();

                int delFlag = targetDestroyed ? 1 : 0;
                fightSectionPacket.setDelFlag((byte) delFlag);
                if (targetDestroyed) {

                    // Last shoot
                    if (targetBattleFort.getFortType().isStation()) {
                        getMatch().getBattleReport().setLastShoot(battleFleet.getGuid());
                    }

                    match.removeFort(targetBattleFort);
                }

                fightSectionPacket.setTargetMatrixId(new CharArray(array));
                battleFleet.update(match);

            }

        }

        if (fightSectionPacket.getSourceDirection() > 3 || fightSectionPacket.getSourceDirection() < 0) {

            fightSectionPacket.setSourceDirection((byte) battleFleet.getDirection());

        }

        fightSectionPacket.setSourceMovePath(new CharArray(movementPath));
        // BotLogger.log("FightSection :: SFSIZE :: " + fightSectionPacket.getShipFights().size() + ", " + fightSectionPacket.getShipFights());

        // match.save(); // Important save for future viewers
        match.sendPacketToViewers(fightSectionPacket);

    }

    public void calculateNextRound() {

        // Add 1 to current round
        match.setRound(match.getRound() + 1);

        // Check fleets ready to be added to the next round
        if (!match.getMatchType().isVirtual()) {

            List<Fleet> start = PacketService.getInstance().getFleetCache().findAllByGalaxyIdAndMatch(getMatch().getGalaxyId(), false)
                    .stream().filter(fleet -> fleet.getFleetTransmission() == null)
                    .collect(Collectors.toList());

            if (!start.isEmpty()) {

                List<Fleet> enemies = start.stream().filter(fleet -> fleet.getFleetInitiator() != null && fleet.getFleetInitiator().getJumpType() == JumpType.ATTACK).collect(Collectors.toList());
                List<Fleet> allies = start.stream().filter(fleet -> !enemies.contains(fleet)).collect(Collectors.toList());

                List<BattleFleet> createdEnemies = BattleService.getInstance().createBattleFleets(enemies, false, match);
                List<BattleFleet> createdAllies = BattleService.getInstance().createBattleFleets(allies, true, match);

                if (!enemies.isEmpty()) {
                    match.getFleets().addAll(createdEnemies);
                }
                if (!allies.isEmpty()) {
                    match.getFleets().addAll(createdAllies);
                }

                int maxRound = this.getMatch().getMaxRound();
                this.getMatch().setMaxRound(Math.min(maxRound + createdAllies.size() + createdEnemies.size(), 100));

                createdEnemies.forEach(battleFleet -> getMatch().getBattleReport().addAttackerSent(battleFleet.getAmount()));
                createdAllies.forEach(battleFleet -> getMatch().getBattleReport().addDefenderSent(battleFleet.getAmount()));

            }

        }

        // Reset actions counter
        match.setAction(0);

        // Reset all fleets
        match.getFleets().stream().forEach(fleet -> {

            fleet.setAnimation(false);
            BattleCommander commander = fleet.getBattleCommander();

            commander.setTemporalDefenseRate(0.0d);
            commander.setTemporalAccuracy(0.0d);
            commander.setTemporalDodge(0.0d);
            commander.setTemporalElectron(0.0d);
            commander.setTemporalSpeed(0.0d);

        });

        // Pass round effects
        for (BattleFleet battleFleet : match.getFleets()) {

            if (battleFleet.isDestroyed()) {
                continue;
            }


            FleetEffects effects = battleFleet.getEffects();
            ShipTechs techs = battleFleet.getTechs();
            BattleCommander commander = battleFleet.getBattleCommander();

            List<FleetEffect> toFleetRemove = battleFleet.getEffects().getEffects().stream()
                    .filter(fleetEffect -> fleetEffect.getUntil() != -1 && match.getRound() > fleetEffect.getUntil())
                    .toList();

            battleFleet.getEffects().getEffects().removeAll(toFleetRemove);

            for (BattleFleetCell battleFleetCell : battleFleet.getCells()) {

                List<ShipEffect> toCellRemove = battleFleetCell.getEffects().getEffects().stream()
                        .filter(shipEffect -> shipEffect.isRemove() || (shipEffect.getUntil() != -1 && match.getRound() > shipEffect.getUntil()))
                        .toList();

                battleFleetCell.getEffects().getEffects().removeAll(toCellRemove);

            }

            // ! Aileen [Skill] #2 ! Bain [Skill] #2
            if (effects.contains(EffectType.AILEEN) || effects.contains(EffectType.BAIN)) {
                commander.setTemporalAccuracy(-commander.getTotalAccuracy());
                commander.setTemporalDodge(-commander.getTotalDodge());
                commander.setTemporalSpeed(-commander.getTotalSpeed());
                commander.setTemporalElectron(-commander.getTotalElectron());
            }

            // Flagship
            ShipStats flagshipStats = new ShipStats();
            flagshipStats.pass(battleFleet);

            // Fleet cells regeneration
            for (BattleFleetCell cell : battleFleet.getCells()) {

                if (!cell.hasShips()) {
                    continue;
                }
                int effectiveStack = commander.getEffectiveStack(cell);

                // ! Ringel [Skill] #2
                if (cell.getEffects().contains(EffectType.RINGEL)) {

                    ShipEffect ringelEffect = cell.getEffects().getEffects().stream()
                            .filter(fleetEffect -> fleetEffect.getEffectType() == EffectType.RINGEL)
                            .findFirst().orElse(null);

                    if (ringelEffect != null) {

                        int amount = (int) ringelEffect.getValue();

                        battleFleet.getEffects().getEffects().remove(ringelEffect);
                        cell.durability(amount);

                    }

                }

                // Chip, gems, tech Regeneration
                double shieldsRegen = (commander.getAdditionalShieldRegeneration() * effectiveStack) * (1 + commander.getShieldIncrement() + techs.getShieldHealRateBonus());
                double armorRegen = (commander.getAdditionalArmorRegeneration() * effectiveStack) * (1 + commander.getStructureIncrement() + techs.getArmorHealRateBonus());

                // ~ allShieldHeal
                if (flagshipStats.getAllShieldHeal() > 0.0) {

                    double allShieldHeal = cell.getMaxShields() * flagshipStats.getAllShieldHeal();
                    shieldsRegen += allShieldHeal;

                }

                shieldsRegen += cell.getMaxShields() * commander.getShieldHealIncrement();

                cell.setShields((int) Math.min(cell.getShields() + shieldsRegen, cell.getMaxShields()));
                cell.setStructure((int) Math.min(cell.getStructure() + armorRegen, cell.getMaxStructure()));

                // Modules Regeneration
                List<BattleFleetDefensiveModule> regenModules = cell.getDefensiveModules().stream()
                        .filter(module -> module.getArmorHeal() > 0 || module.getShieldHeal() > 0)
                        .toList();

                for (BattleFleetDefensiveModule defensiveModule : regenModules) {

                    if (battleFleet.getHe3() < (defensiveModule.getFuelUsage() * effectiveStack)) {
                        continue;
                    }
                    battleFleet.setHe3(Math.max(battleFleet.getHe3() - (defensiveModule.getFuelUsage() * effectiveStack), 0));

                    double armorHeal = defensiveModule.getArmorHeal();
                    double shieldHeal = defensiveModule.getShieldHeal();

                    cell.setStructure((int) Math.min(cell.getStructure() + (armorHeal * effectiveStack), cell.getMaxStructure()));
                    cell.setShields((int) Math.min(cell.getShields() + (shieldHeal * effectiveStack), cell.getMaxShields()));

                }

            }

            // Check skills
            {

                // ! Natiya [Skill]
                if (battleFleet.isCommanded("commander:natiya")) {
                    if (battleFleet.trigger("commander:natiya")) {
                        battleFleet.setAnimation(true);
                        commander.setTemporalSpeed(commander.getTemporalSpeed() + (commander.getLevel() * (commander.getStars() + 1)));
                    }
                }

                // ! Vinna [Skill] #1
                if (battleFleet.isCommanded("commander:vinna")) {

                    FleetEffect vinnaEffect = battleFleet.getEffects().getEffects().stream()
                            .filter(fleetEffect -> fleetEffect.getEffectType() == EffectType.VINNA)
                            .findFirst().orElse(null);

                    if (vinnaEffect != null) {

                        int amount = (int) vinnaEffect.getValue();
                        battleFleet.getEffects().getEffects().remove(vinnaEffect);

                        while (amount >= 1) {

                            amount--;
                            int random = MathUtil.randomInclusive(1, 4);

                            switch (random) {
                                case 1: // accuracy
                                    commander.setTotalAccuracy(commander.getTotalAccuracy() + 10.0);
                                    break;
                                case 2: // dodge
                                    commander.setTotalDodge(commander.getTotalDodge() + 10.0);
                                    break;
                                case 3: // electron
                                    commander.setTotalElectron(commander.getTotalElectron() + 10.0);
                                    break;
                                default: // speed
                                    commander.setTotalSpeed(commander.getTotalSpeed() + 10.0);
                                    break;
                            }

                        }

                    }

                }

            }

            commander.setTrigger(CommanderService.getInstance().createTrigger(commander));

        }

        // Get all fleets sorted by speed
        List<BattleElement> temporalElements = match.getTemporalElementsSorted();

        // Separate fleets and fortifications
        List<BattleFleet> temporalFleets = temporalElements.stream().filter(element -> element instanceof BattleFleet).map(element -> (BattleFleet) element).collect(Collectors.toList());
        List<BattleFort> temporalForts = temporalElements.stream().filter(element -> element instanceof BattleFort).map(element -> (BattleFort) element).collect(Collectors.toList());

        // Set current round
        match.setCurrentRound(BattleRound.builder()
                .roundId(match.getRound())
                .fleets(temporalFleets)
                .forts(temporalForts)
                .actions(new ArrayList<>())
                .build());

        temporalFleets.forEach(BattleFleet::recalculate);

        for (BattleFort fort : temporalForts) {

            calculateAction(fort, temporalElements);

        }

        for (BattleFleet fleet : temporalFleets) {
            var loop = false;
            double lastAttackInRow = 0;
            do {
                calculateAction(fleet, temporalElements, temporalFleets);
                //calc eva should attack the next target
                var evaAttack = fleet.getEvaAttackRows();
                if (evaAttack != null) {
                    if (lastAttackInRow == evaAttack) {
                        break;
                    }
                    var total = fleet.getBattleCommander().getTotalAccuracy() + fleet.getBattleCommander().getTotalElectron();
                    if (total <= 400) {
                        loop = evaAttack < 2;
                    } else if (total <= 1000) {
                        loop = evaAttack < 3;
                    } else if (total <= 1800) {
                        loop = evaAttack < 4;
                    } else {
                        loop = evaAttack < 5;
                    }
                    lastAttackInRow = evaAttack;
                    if (loop) {
                        fleet.setAnimation(true);
                    }
                }
            }
            while (loop);
        }
        BattleService.getInstance().sendMatchRoundToViewers(match);
        // match.save();

        actionFinishDate = new Date().getTime() + 1000L;

    }

    @Builder
    @Getter
    public static class AreaEffect {
        private AreaEffectType type;
        private Integer sourceShipTeamId;
        private List<Integer> targetShipTeamIds;

        public enum AreaEffectType {
            REDUCTION(0.05d),
            ADDITION(0.08d),
            REGEN(0.75d),
            REGEN_FULL(1.0d),
            REGEN_SHIELD(0.15d),
            REGEN_STRUCTURE(0.15d),
            LUNA_REDUCTION(0.9d),

            ;

            private final double value;

            AreaEffectType(double value) {
                this.value = value;
            }
        }
    }

    public void doAreaEffect(AreaEffect.AreaEffectType effectType, BattleFleetCell cell) {
        switch (effectType) {
            case REDUCTION, LUNA_REDUCTION -> {
                double reduction = effectType.value;
                cell.structure(cell.getStructure() * reduction);
                cell.shields(cell.getShields() * reduction);
            }
            case REGEN, REGEN_FULL, ADDITION -> {
                double regen = effectType.value;
                cell.setShields((int) Math.min(Math.max(cell.getShields() + (cell.getMaxShields() * regen), 0), cell.getMaxShields()));
                cell.setStructure((int) Math.min(Math.max(cell.getStructure() + (cell.getMaxStructure() * regen), 0), cell.getMaxStructure()));
            }
            case REGEN_SHIELD -> {
                double regen = effectType.value;
                cell.setShields((int) Math.min(Math.max(cell.getShields() + (cell.getMaxShields() * regen), 0), cell.getMaxShields()));
            }
            case REGEN_STRUCTURE -> {
                double regen = effectType.value;
                cell.setStructure((int) Math.min(Math.max(cell.getStructure() + (cell.getMaxStructure() * regen), 0), cell.getMaxStructure()));
            }
        }
    }

    public void calculateAction(BattleFleet battleFleet, List<BattleElement> temporalElements, List<BattleFleet> temporalFleets) {

        if (battleFleet.isDestroyed()) {
            return;
        }

        BattleAction action = BattleAction.builder()
                .type("action:ship.team.procedure")
                .involvedId(battleFleet.getShipTeamId())
                .actionId(match.getCurrentRound().calculateNextActionId())
                .triggers(new LinkedList<>())
                .build();

        action.addTrigger(StartTrigger.builder()
                .id(action.calculateNextTriggerId())
                .type("trigger:start")
                .shipTeamId(battleFleet.getShipTeamId())
                .build());

        BattleCell[][] cells = getCells(temporalElements);
        BattleElement target = null; // Calculate the best target for this battleShip (this is the goal to attack)

        // Only calculate target if the current one is not destroyed
        target = calculateTarget(battleFleet, temporalElements);
        if (target == null) {
            return;
        }

        switch (target.getType()) {

            case FLEET:

                BattleFleet targetFleet = (BattleFleet) target;

                // Put a trigger for keep target ship team id along rounds
                // and avoid changing the target every round.
                action.addTrigger(FleetMarkTargetTrigger.builder()
                        .id(action.calculateNextTriggerId())
                        .type("trigger:target")
                        .millis(0L)
                        .attackerShipTeamId(battleFleet.getShipTeamId())
                        .elementType(BattleElementType.FLEET)
                        .targetId(targetFleet.getShipTeamId())
                        .build());

                break;

            case FORTIFICATION:

                if (battleFleet.getFleetTargetInterval().isFleetsPriority()) {
                    break;
                }

                BattleFort targetFort = (BattleFort) target;

                // Put a trigger for keep target ship team id along rounds
                // and avoid changing the target every round.
                action.addTrigger(FleetMarkTargetTrigger.builder()
                        .id(action.calculateNextTriggerId())
                        .type("trigger:target")
                        .millis(0L)
                        .attackerShipTeamId(battleFleet.getShipTeamId())
                        .elementType(BattleElementType.FORTIFICATION)
                        .targetId(targetFort.getFortId())
                        .build());

                break;

        }

        int movement = battleFleet.calculateMovement();

        // ? Techs
        if (battleFleet.getEffects().contains(EffectType.DYNAMIC_IMPAIRMENT)) {
            movement -= battleFleet.getEffects().getEffect(EffectType.DYNAMIC_IMPAIRMENT).getValue();
        }

        // ? Flagship
        ShipStats flagshipStats = new ShipStats();
        flagshipStats.pass(battleFleet);

        if (flagshipStats.getAllMovementBonus() > 0) {
            movement = (int) Math.min(movement + flagshipStats.getAllMovementBonus(), 16);
        }

        BattleCommander commander = battleFleet.getBattleCommander();

        List<AreaEffect> areaEffects = new ArrayList<>();

        // Commander Skills
        {
            // ! Frontline Surge [Skill]
            if (battleFleet.isCommanded("commander:frontlineSurge")) {
                if (battleFleet.trigger("commander:frontlineSurge")) {
                    movement = Math.min(movement + 3, 16);
                    battleFleet.setAnimation(true);
                }
            }

            // ! Sofia [Skill]
            if (battleFleet.isCommanded("commander:sofia")) {
                if (battleFleet.trigger("commander:sofia")) {
                    movement = Math.min(movement + 3, 16);
                    battleFleet.setAnimation(true);
                }
            }

            // ! Krina Klaus [Skill]
            if (battleFleet.isCommanded("commander:krinaKlaus")) {
                if (battleFleet.trigger("commander:krinaKlaus")) {
                    battleFleet.setAnimation(true);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            other.getBattleCommander().addTemporalDefenseRate(10);
                        }
                    }
                }
            }

            // ! Iron Maidens [Skill] #2
            if (battleFleet.isCommanded("commander:ironMaidens")) {
                if (battleFleet.trigger("commander:ironMaidens")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REGEN_FULL)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(List.of(battleFleet.getShipTeamId()))
                            .build();
                    for (BattleFleetCell fleetCell : battleFleet.getCells()) {
                        doAreaEffect(AreaEffect.AreaEffectType.REGEN_FULL, fleetCell);
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Enduring Chorus [Skill]
            if (battleFleet.isCommanded("commander:enduringChorus")) {
                if (battleFleet.trigger("commander:enduringChorus")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REGEN_FULL)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(List.of(battleFleet.getShipTeamId()))
                            .build();
                    for (BattleFleetCell fleetCell : battleFleet.getCells()) {
                        doAreaEffect(AreaEffect.AreaEffectType.REGEN_FULL, fleetCell);
                    }
                    areaEffects.add(areaEffect);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGuid() != battleFleet.getGuid()) {
                            // dont buff fleets that are not of the same player
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            other.getBattleCommander().addTemporalDodge(5);
                        }
                    }
                }
            }

            // ! Dilira [Skill]
            if (battleFleet.isCommanded("commander:dilira")) {
                if (battleFleet.trigger("commander:dilira")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REGEN_FULL)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(List.of(battleFleet.getShipTeamId()))
                            .build();
                    for (BattleFleetCell fleetCell : battleFleet.getCells()) {
                        doAreaEffect(AreaEffect.AreaEffectType.REGEN_FULL, fleetCell);
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Leech Lurkers [Skill]
            if (battleFleet.isCommanded("commander:leechLurkers")) {
                if (battleFleet.trigger("commander:leechLurkers")) {

                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.BART)
                            .value(-1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);

                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REDUCTION)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (!other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (!battleFleet.canAffect(other)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 2) {
                            areaEffect.getTargetShipTeamIds().add(other.getShipTeamId());
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.REDUCTION, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Lurking Light [Skill]
            if (battleFleet.isCommanded("commander:lurkingLight")) {
                if (battleFleet.trigger("commander:lurkingLight")) {

                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.BART)
                            .value(-1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.ADDITION)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();

                    battleFleet.setAnimation(true);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            areaEffect.getTargetShipTeamIds().add(other.getShipTeamId());
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.ADDITION, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Winter Knights [Skill]
            if (battleFleet.isCommanded("commander:winterKnights")) {
                if (battleFleet.trigger("commander:winterKnights")) {
                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.BART)
                            .value(-1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            other.getBattleCommander().addTemporalDefenseRate(10);
                        }
                    }
                }
            }

            // ! Queens of Blades [Skill] #2
            if (battleFleet.isCommanded("commander:queensOfBlades")) {
                if (battleFleet.trigger("commander:queensOfBlades")) {
                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.BART)
                            .value(-1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                }
            }

            // ! Bart [Skill]
            if (battleFleet.isCommanded("commander:bart")) {
                if (battleFleet.trigger("commander:bart")) {
                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.BART)
                            .value(-1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                }
            }

            // ! Light & Darkness [Skill]
            if (battleFleet.isCommanded("commander:lightAndDarkness")) {
                if (battleFleet.trigger("commander:lightAndDarkness")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.ADDITION)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            areaEffect.getTargetShipTeamIds().add(other.getShipTeamId());
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.ADDITION, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect);

                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect2 = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REDUCTION)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (!other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (!battleFleet.canAffect(other)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 2) {
                            areaEffect2.getTargetShipTeamIds().add(other.getShipTeamId());
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.REDUCTION, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect2);
                }
            }

            // ! Annata [Skill]
            if (battleFleet.isCommanded("commander:annata")) {
                if (battleFleet.trigger("commander:annata")) {
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.ADDITION)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();
                    battleFleet.setAnimation(true);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            areaEffect.getTargetShipTeamIds().add(other.getShipTeamId());
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.ADDITION, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Leo [Skill]
            if (battleFleet.isCommanded("commander:leo")) {
                if (battleFleet.trigger("commander:leo")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REDUCTION)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (!other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (!battleFleet.canAffect(other)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 2) {
                            areaEffect.getTargetShipTeamIds().add(other.getShipTeamId());
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.REDUCTION, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Murphy Lawson [Skill]
            if (battleFleet.isCommanded("commander:murphyLawson")) {
                if (battleFleet.trigger("commander:murphyLawson")) {
                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.MURPHY_LAWSON)
                            .value(1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                }
            }

            // ! Maxius [Skill]
            if (battleFleet.isCommanded("commander:maxius")) {
                if (battleFleet.trigger("commander:maxius")) {
                    battleFleet.setAnimation(true);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGuid() != battleFleet.getGuid()) {
                            // dont buff fleets that are not of the same player
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            other.getBattleCommander().addTemporalAccuracy(5);
                        }
                    }
                }
            }

            // ! Shadow Countess [Skill] #1
            if (battleFleet.isCommanded("commander:shadowCountess")) {
                if (battleFleet.trigger("commander:shadowCountess")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REGEN)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(new ArrayList<>())
                            .build();
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            areaEffect.getTargetShipTeamIds().add(other.getShipTeamId());
                            if (!other.getEffects().contains(EffectType.SHADOW_COUNTESS)) {
                                FleetEffect effect = FleetEffect.builder()
                                        .effectType(EffectType.SHADOW_COUNTESS)
                                        .value(1)
                                        .until(match.getRound())
                                        .build();
                                other.process(effect);
                                other.getBattleCommander().addTemporalDefenseRate(35);
                            }
                            for (BattleFleetCell cell : other.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.REGEN, cell);
                            }
                        }
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Shaba [Skill]
            if (battleFleet.isCommanded("commander:shaba")) {
                if (battleFleet.trigger("commander:shaba")) {
                    battleFleet.setAnimation(true);
                    for (BattleFleet other : temporalFleets) {
                        if (other == battleFleet) {
                            continue;
                        }
                        if (other.isEnemy(battleFleet)) {
                            continue;
                        }
                        if (other.getGuid() != battleFleet.getGuid()) {
                            // dont buff fleets that are not of the same player
                            continue;
                        }
                        if (other.getGO2Node().getHeuristic(battleFleet.getGO2Node()) <= 4) {
                            other.getBattleCommander().addTemporalDodge(5);
                        }
                    }
                }
            }

            // ! Chrome Dome [Skill] #1
            if (battleFleet.isCommanded("commander:chromeDome")) {
                if (battleFleet.trigger("commander:chromeDome")) {
                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.CHROME_DOME)
                            .value(1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                }
            }

            // ! Luna Silvestri [Skill] #1
            if (battleFleet.isCommanded("commander:lunaSilvestri")) {
                if (battleFleet.trigger("commander:lunaSilvestri")) {
                    battleFleet.setAnimation(true);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.LUNA_SILVESTRI)
                            .value(1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                }
            }

            // ! Slayer Bael [Skill] #1
            if (battleFleet.isCommanded("commander:slayerBael")) {
                if (battleFleet.trigger("commander:slayerBael")) {
                    battleFleet.setAnimation(true);
                    commander.addTemporalAccuracy(500);
                    FleetEffect effect = FleetEffect.builder()
                            .effectType(EffectType.SLAYER_BAEL)
                            .value(-1)
                            .until(match.getRound() + 1)
                            .build();
                    battleFleet.process(effect);
                }
            }

            // ! Rayllf [Skill]
            if (battleFleet.isCommanded("commander:rayllf")) {
                if (battleFleet.trigger("commander:rayllf")) {
                    battleFleet.setAnimation(true);
                    commander.addTemporalDefenseRate(5);
                }
            }

            // ! Kelly [Skill]
            if (battleFleet.isCommanded("commander:kelly")) {
                if (battleFleet.trigger("commander:kelly")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REGEN_STRUCTURE)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(List.of(battleFleet.getShipTeamId()))
                            .build();
                    for (BattleFleetCell fleetCell : battleFleet.getCells()) {
                        doAreaEffect(AreaEffect.AreaEffectType.REGEN_STRUCTURE, fleetCell);
                    }
                    areaEffects.add(areaEffect);
                }
            }

            // ! Donna [Skill]
            if (battleFleet.isCommanded("commander:donna")) {
                if (battleFleet.trigger("commander:donna")) {
                    battleFleet.setAnimation(true);
                    AreaEffect areaEffect = AreaEffect.builder()
                            .type(AreaEffect.AreaEffectType.REGEN_SHIELD)
                            .sourceShipTeamId(battleFleet.getShipTeamId())
                            .targetShipTeamIds(List.of(battleFleet.getShipTeamId()))
                            .build();
                    for (BattleFleetCell fleetCell : battleFleet.getCells()) {
                        doAreaEffect(AreaEffect.AreaEffectType.REGEN_SHIELD, fleetCell);
                    }
                    areaEffects.add(areaEffect);
                }
            }
        }
        action.addAreaEffects(areaEffects);

        movement = Math.max(movement, 1);
        movement = Math.min(movement, 16);

        int minRange = battleFleet.getMinRange();
        int maxRange = battleFleet.getMaxRange();

        Target config = battleFleet.getFleetTarget();
        GO2Path path = Pathfinder.get2Pathing(battleFleet.getCell(cells), target.getCell(cells), getMatch().getBlocks(battleFleet, cells), minRange, maxRange, movement, config);

        // BotLogger.log("Pathing of " + battleFleet.getGuid() + " : " + path);

        // Check if the first path index is the same as fromNode
        // Also value movement trigger for every node path
        // Only move when has HE3 and has attack modules
        if (path != null && battleFleet.getHe3() > 0 && (minRange > 0 && maxRange > 0)) {
            // restrict paths by movement
            int mov = (int) Math.min(movement + flagshipStats.getAllMovementBonus(), 16);
            for (GO2Node node : path.getNodes()) {
                if (mov <= 0) {
                    break;
                }

                action.addTrigger(MovementTrigger.builder()
                        .id(action.calculateNextTriggerId())
                        .type("trigger:movement")
                        .millis(500L)
                        .shipTeamId(battleFleet.getShipTeamId())
                        .fromX(battleFleet.getNode().getX())
                        .fromY(battleFleet.getNode().getY())
                        .sourceAnimation(battleFleet.isAnimation())
                        .toX(node.getX())
                        .toY(node.getY())
                        .build());

                battleFleet.setPosX(node.getX());
                battleFleet.setPosY(node.getY());

                int direction = BattleService.getSmartDirection(battleFleet.getGO2Node(), node, battleFleet.getDirection());

                FleetRotateTrigger rotateTrigger = FleetRotateTrigger.builder()
                        .type("trigger:fleet.rotate")
                        .millis(0L)
                        .guid(battleFleet.getGuid())
                        .shipTeamId(battleFleet.getShipTeamId())
                        .direction(direction)
                        .build();

                battleFleet.setDirection(direction);
                action.addTrigger(rotateTrigger);
                mov--;
            }
        }

        BattleElement priorityTarget = target;

        // Check if we can reach the current target, if not let's attack another thing
        // in the meantime
        if (!battleFleet.canAttack(target)) {
            target = calculateMeantimeTarget(battleFleet, temporalElements, minRange, maxRange);
        }

        if (target != null) {
            BotLogger.dev(battleFleet.getGuid() + " (" + battleFleet.getBattleCommander().getNameId() + ") meantime is: " + target.getNode());
        } else {
            BotLogger.dev(battleFleet.getGuid() + " (" + battleFleet.getBattleCommander().getNameId() + ") meantime is: null");
        }

        // If after meantime attack we can't found something to attack
        // target we need to be sure that target is not null
        if (target == null) {

            if (battleFleet.getHe3() > 0 && (minRange > 0 && maxRange > 0)) {

                int direction = BattleService.getSmartDirection(battleFleet.getGO2Node(), priorityTarget.getGO2Node(), battleFleet.getDirection());

                FleetRotateTrigger rotateTrigger = FleetRotateTrigger.builder()
                        .type("trigger:fleet.rotate")
                        .millis(0L)
                        .guid(battleFleet.getGuid())
                        .shipTeamId(battleFleet.getShipTeamId())
                        .direction(direction)
                        .build();

                battleFleet.setDirection(direction);
                action.addTrigger(rotateTrigger);

            }

            action.addTrigger(EndTrigger.builder()
                    .id(action.calculateNextTriggerId())
                    .type("trigger:end")
                    .millis(0L)
                    .shipTeamId(battleFleet.getShipTeamId())
                    .build());

            match.getCurrentRound().getActions().add(action);
            return;

        }

        // Steps
        int steps = path == null ? 0 : path.getNodes().size();

        if (path != null) {
            BotLogger.dev(battleFleet.getGuid() + " (" + battleFleet.getBattleCommander().getNameId() + ") steps are: " + steps + ", turns are: " + path.turns());
        } else {
            BotLogger.dev(battleFleet.getGuid() + " (" + battleFleet.getBattleCommander().getNameId() + ") steps & turns are: 0");
        }

        // Can Attack (max range >= distance between two fleets: attacker and target)
        if (battleFleet.canAttack(target) && battleFleet.getHe3() > 0) {

            BotLogger.dev(battleFleet.getGuid() + " (" + battleFleet.getBattleCommander().getNameId() + ") can attack: " + true);

            switch (target.getType()) {

                case FLEET -> {

                    BattleFleet targetFleet = (BattleFleet) target;

                    // todo
                    // Check parts cooldown
                    // Check cmds skills
                    // and moar
                    FleetAttackFleetTrigger attackTrigger = FleetAttackFleetTrigger.builder()
                            .type("trigger:fleet.attack.fleet")
                            .millis(0L)
                            .attackerShipTeamId(battleFleet.getShipTeamId())
                            .defenderShipTeamId(targetFleet.getShipTeamId())
                            .attacks(new ArrayList<>())
                            .fleetEffects(new ArrayList<>())
                            .shipAttackPacketActions(new ArrayList<>())
                            .build();

                    // Rotate ship to aim to the enemy fleet
                    int direction = BattleService.getSmartDirection(battleFleet.getGO2Node(), target.getGO2Node(), battleFleet.getDirection());
                    battleFleet.setDirection(direction);

                    // ! Luna Silvestri [Skill] #2
                    if (battleFleet.getEffects().contains(EffectType.LUNA_SILVESTRI)) {
                        if (battleFleet.canAffect(targetFleet)) {
                            for (BattleFleetCell cell : targetFleet.getCells()) {
                                if (!cell.hasShips()) {
                                    continue;
                                }
                                if (!targetFleet.isEnemy(battleFleet)) {
                                    continue;
                                }
                                if (!battleFleet.canAffect(targetFleet)) {
                                    continue;
                                }
                                doAreaEffect(AreaEffect.AreaEffectType.LUNA_REDUCTION, cell);
                            }
                            action.addAreaEffect(AreaEffect.builder()
                                    .type(AreaEffect.AreaEffectType.LUNA_REDUCTION)
                                    .sourceShipTeamId(battleFleet.getShipTeamId())
                                    .targetShipTeamIds(List.of(targetFleet.getShipTeamId()))
                                    .build());
                        }

                    }

                    boolean successAttack = attackFleetByFleet(steps, battleFleet, targetFleet, attackTrigger);

                    if (successAttack) {

                        action.addTrigger(attackTrigger);

                        int repel = attackTrigger.getRepelSteps();

                        if (repel > 0) {

                            int realFallbackSteps = 0;

                            int oldX = targetFleet.getPosX();
                            int oldY = targetFleet.getPosY();

                            switch (attackTrigger.getAttackerDirection()) {
                                case 0:
                                    targetFleet.setPosX(Math.max(0, Math.min(targetFleet.getPosX() + repel, 24)));
                                    realFallbackSteps = Math.abs(targetFleet.getPosX() - oldX);
                                    // System.out.println("1: " + realFallbackSteps + ", " + targetFleet.getPosX() + ", " + oldX);
                                    break;
                                case 1:
                                    targetFleet.setPosY(Math.max(0, Math.min(targetFleet.getPosY() + repel, 24)));
                                    realFallbackSteps = Math.abs(targetFleet.getPosY() - oldY);
                                    // System.out.println("2: " + realFallbackSteps + ", " + targetFleet.getPosY() + ", " + oldY);
                                    break;
                                case 2:
                                    targetFleet.setPosX(Math.max(0, Math.min(targetFleet.getPosX() - repel, 24)));
                                    realFallbackSteps = Math.abs(targetFleet.getPosX() - oldX);
                                    // System.out.println("3: " + realFallbackSteps + ", " + targetFleet.getPosX() + ", " + oldX);
                                    break;
                                default:
                                    targetFleet.setPosY(Math.max(0, Math.min(targetFleet.getPosY() - repel, 24)));
                                    realFallbackSteps = Math.abs(targetFleet.getPosY() - oldY);
                                    // System.out.println("4: " + realFallbackSteps + ", " + targetFleet.getPosY() + ", " + oldY);
                                    break;
                            }

                            // System.out.println("Fallback steps: " + realFallbackSteps + ", From: " + repel);
                            attackTrigger.setRepelSteps(realFallbackSteps);

                        }
                        if (battleFleet.isCommanded("commander:eva")) {
                            if (targetFleet.isDestroyed()) {
                                // ! Eva [Skill]
                                FleetEffect effect = FleetEffect.builder()
                                        .effectType(EffectType.Eva)
                                        .value(1)
                                        .until(match.getRound())
                                        .build();
                                battleFleet.process(effect);
                            } else {
                                //add until it exceed the max count and disable the loop
                                FleetEffect effect = FleetEffect.builder()
                                        .effectType(EffectType.Eva)
                                        .value(100)
                                        .until(match.getRound())
                                        .build();
                                battleFleet.process(effect);
                            }
                        }


                    }

                    FleetRotateTrigger rotateTrigger = FleetRotateTrigger.builder()
                            .type("trigger:fleet.rotate")
                            .millis(0L)
                            .guid(battleFleet.getGuid())
                            .shipTeamId(battleFleet.getShipTeamId())
                            .direction(direction)
                            .build();

                    action.addTrigger(rotateTrigger);

                }

                case FORTIFICATION -> {

                    BattleFort targetFort = (BattleFort) target;

                    // todo
                    // Check parts cooldown
                    // Check cmds skills
                    // and moar
                    FleetAttackFortTrigger attackTrigger = FleetAttackFortTrigger.builder()
                            .type("trigger:fleet.attack.fort")
                            .millis(0L)
                            .attackerShipTeamId(battleFleet.getShipTeamId())
                            .defenderFortId(targetFort.getFortId())
                            .attacks(new ArrayList<>())
                            .shipAttackPacketActions(new ArrayList<>())
                            .build();

                    // Rotate ship to aim to the enemy fleet
                    int direction = BattleService.getSmartDirection(battleFleet.getGO2Node(), target.getGO2Node(), battleFleet.getDirection());
                    battleFleet.setDirection(direction);

                    boolean successAttack = attackFortByFleet(battleFleet, targetFort, attackTrigger);
                    if (successAttack) {

                        attackTrigger.setMillis(1000L);
                        action.addTrigger(attackTrigger);

                    }

                    FleetRotateTrigger rotateTrigger = FleetRotateTrigger.builder()
                            .type("trigger:fleet.rotate")
                            .millis(0L)
                            .guid(battleFleet.getGuid())
                            .shipTeamId(battleFleet.getShipTeamId())
                            .direction(direction)
                            .build();

                    action.addTrigger(rotateTrigger);

                }

            }

        }

        BotLogger.dev(battleFleet.getGuid() + " (" + battleFleet.getBattleCommander().getNameId() + ") ending");

        action.addTrigger(EndTrigger.builder()
                .id(action.calculateNextTriggerId())
                .type("trigger:end")
                .millis(2000L)
                .shipTeamId(battleFleet.getShipTeamId())
                .build());

        match.getCurrentRound().addAction(action);

        BotLogger.dev("--------------------");

    }

    public void calculateAction(BattleFort battleFort, List<BattleElement> temporalElements) {

        if (battleFort.isDestroyed() || !battleFort.hasCannon()) {
            return;
        }
        if (battleFort.getNextAttack() > getRound()) {
            //reloading
            return;
        }
        BattleAction action = BattleAction.builder()
                .type("action:fort.procedure")
                .involvedId(battleFort.getFortId())
                .actionId(match.getCurrentRound().calculateNextActionId())
                .triggers(new LinkedList<>())
                .build();

        action.addTrigger(StartFortTrigger.builder()
                .id(action.calculateNextTriggerId())
                .type("trigger:start")
                .fortId(battleFort.getFortId())
                .build());

        List<BattleFleet> targets = new ArrayList<>();

        // Check if we can reach the current target
        switch (battleFort.getAttackType()) {

            case ALL_ATTACK -> {
                List<BattleFleet> attackable;
                if (battleFort.isStation()) {
                    attackable = temporalElements.stream()
                            .filter(element -> element.getType() == BattleElementType.FLEET)
                            .map(element -> (BattleFleet) element)
                            .filter(fleet -> !fleet.isDestroyed()).toList();
                } else {
                    attackable = temporalElements.stream()
                            .filter(element -> element.getType() == BattleElementType.FLEET)
                            .map(element -> (BattleFleet) element)
                            .filter(fleet -> !fleet.isDestroyed() && match.fortressAttackType().isAttacker() == fleet.isAttacker()).toList();
                }
                List<BattleFleet> reachedTargets = new ArrayList<>();
                for (BattleFleet target : attackable) {
                    int distance = target.getGO2Node().getHeuristic(battleFort.getGO2Node());
                    if (distance <= battleFort.getRange()) {
                        reachedTargets.add(target);
                    }
                }
                targets = reachedTargets;
            }

            case RADIUS_TARGET -> {
                List<BattleFleet> attackable = temporalElements.stream()
                        .filter(element -> element.getType() == BattleElementType.FLEET)
                        .map(element -> (BattleFleet) element)
                        .filter(fleet -> !fleet.isDestroyed() && match.fortressAttackType().isAttacker() == fleet.isAttacker()).toList();
                if (attackable.isEmpty()) {
                    return;
                }
                List<BattleFleet> reachedTargets = new ArrayList<>();
                BattleFleet reached = null;
                for (BattleFleet target : attackable) {
                    int distance = target.getGO2Node().getHeuristic(battleFort.getGO2Node());
                    if (distance <= battleFort.getRange()) {
                        reached = target;
                        break;
                    }
                }
                if (reached == null) {
                    break;
                }
                for (BattleFleet target : attackable) {
                    int distance = target.getGO2Node().getHeuristic(reached.getGO2Node());
                    if (distance <= 1) {
                        reachedTargets.add(target);
                    }
                }
                reachedTargets.add(reached);
                targets = reachedTargets;
            }

            case SINGLE_TARGET -> {
                List<BattleFleet> attackable = temporalElements.stream()
                        .filter(element -> element.getType() == BattleElementType.FLEET)
                        .map(element -> (BattleFleet) element)
                        .filter(fleet -> !fleet.isDestroyed() && match.fortressAttackType().isAttacker() == fleet.isAttacker()).toList();
                if (attackable.isEmpty()) {
                    return;
                }
                BattleFleet reached = null;
                for (BattleFleet target : attackable) {
                    int distance = target.getGO2Node().getHeuristic(battleFort.getGO2Node());
                    if (distance <= battleFort.getRange()) {
                        reached = target;
                        break;
                    }
                }
                if (reached != null) {
                    targets = List.of(reached);
                }
            }
        }

        if (targets.isEmpty()) {
            return;
        }
        boolean attack = false;

        for (BattleFleet targetFleet : targets) {

            FortAttackFleetTrigger attackTrigger = FortAttackFleetTrigger.builder()
                    .type("trigger:fort.attack.fleet")
                    .millis(0L)
                    .attackerFortId(battleFort.getFortId())
                    .attacks(new ArrayList<>())
                    .shipAttackPacketActions(new ArrayList<>())
                    .build();

            boolean successAttack = attackFleetByFort(battleFort, targetFleet, attackTrigger);

            if (successAttack) {
                attack = true;
                battleFort.getMeta().getEffect("backfill").ifPresent(backfill -> attackTrigger.setNextAttack(getRound() + 1 + backfill.getValue()));
                action.addTrigger(attackTrigger);
            }

        }
        action.addTrigger(EndFortTrigger.builder()
                .id(action.calculateNextTriggerId())
                .type("trigger:end")
                .millis(attack ? 1350L : 0L)
                .fortId(battleFort.getFortId())
                .build());

        match.getCurrentRound().getActions().add(action);

    }

    public BattleElement calculateTarget(BattleFleet battleFleet, List<BattleElement> temporalElements) {

        FleetEffects fleetEffects = battleFleet.getEffects();
        List<BattleElement> result = new ArrayList<>();
        boolean onlyFort = true;

        if (fleetEffects.contains(EffectType.STANI, EffectType.MEDUSA)) {
            return null;
        }

        boolean lonely = temporalElements.stream()
                .filter(element -> element.getType() == BattleElementType.FLEET)
                .map(element -> (BattleFleet) element)
                .filter(fleet -> fleet.isEnemy(battleFleet))
                .count() == 1;

        for (BattleElement element : temporalElements) {
            if (element.getType() == BattleElementType.FORTIFICATION) {

                BattleFort battleFort = (BattleFort) element;

                if (battleFort.isDestroyed()) {
                    continue;
                }
                if (battleFleet.isAttacker() && match.fortressAttackType() != AttackSideType.ATTACKER) {
                    continue;
                }
                if (battleFleet.isDefender() && match.fortressAttackType() != AttackSideType.DEFENDER) {
                    continue;
                }

                result.add(element);

            } else {

                BattleFleet fleet = (BattleFleet) element;
                if (fleet.isDestroyed()) {
                    continue;
                }

                if (fleet.isEnemy(battleFleet) && !fleet.isDestroyed()) {
                    onlyFort = false;
                    if (fleet.getEffects().contains(EffectType.BART) && !lonely) {
                        if (battleFleet.isNullified() || fleet.canAffect(battleFleet)) {
                            continue;
                        }
                    }

                    result.add(fleet);
                    continue;

                }

            }
        }

        TargetInterval targetInterval = battleFleet.getFleetTargetInterval();
        if (onlyFort) {
            targetInterval = TargetInterval.CLOSEST;
        }
        IntervalComparator comparator = targetInterval.getComparator(battleFleet);
        result.sort(comparator);

        if (result.isEmpty()) {
            return null;
        }

        if (battleFleet.getFleetTargetInterval().isFleetsPriority()) {
            result = result.stream().sorted((element1, element2) -> {
                if (element1.getType() == BattleElementType.FLEET && element2.getType() == BattleElementType.FORTIFICATION) {
                    return -1;
                }
                if (element2.getType() == BattleElementType.FLEET && element1.getType() == BattleElementType.FORTIFICATION) {
                    return 1;
                }
                return 0;
            }).collect(Collectors.toList());
        }

        BattleElement selected = result.get(0);

        if (selected instanceof BattleFort && result.size() > 1) {
            if (((BattleFort) selected).getFortType().isStation()) {
                selected = result.get(1);
            }
        }

        return selected;

    }

    public BattleElement calculateMeantimeTarget(BattleFleet battleFleet, List<BattleElement> temporalElements, int minRange, int maxRange) {

        FleetEffects fleetEffects = battleFleet.getEffects();
        List<BattleElement> result = new ArrayList<>();

        if (fleetEffects.contains(EffectType.STANI, EffectType.MEDUSA)) {
            return null;
        }

        boolean lonely = temporalElements.stream()
                .filter(element -> element.getType() == BattleElementType.FLEET)
                .map(element -> (BattleFleet) element)
                .filter(fleet -> fleet.isEnemy(battleFleet))
                .count() == 1;

        Node node = battleFleet.getNode();

        for (BattleElement element : temporalElements) {
            if (element.getType() == BattleElementType.FORTIFICATION) {

                BattleFort fort = (BattleFort) element;

                if (fort.isDestroyed()) {
                    continue;
                }
                if (battleFleet.isAttacker() && match.fortressAttackType() != AttackSideType.ATTACKER) {
                    continue;
                }
                if (battleFleet.isDefender() && match.fortressAttackType() != AttackSideType.DEFENDER) {
                    continue;
                }

                int distance = node.getHeuristic(fort.getNode());
                if (!(distance >= minRange && distance <= maxRange)) {
                    continue;
                }
                if (fort.getFortType().isStation()) {
                    continue;
                }

                result.add(fort);
                continue;

            } else {

                BattleFleet fleet = (BattleFleet) element;

                if (fleet.isEnemy(battleFleet) && !fleet.isDestroyed()) {

                    int distance = node.getHeuristic(fleet.getNode());

                    if (fleet.getEffects().contains(EffectType.BART) && !lonely) {
                        if (battleFleet.isNullified() || fleet.canAffect(battleFleet)) {
                            continue;
                        }
                    }

                    if (!(distance >= minRange && distance <= maxRange)) {
                        continue;
                    }

                    if (fleet.isAttacker() && battleFleet.isAttacker()) {
                        continue;
                    }

                    if (fleet.isDefender() && battleFleet.isDefender()) {
                        continue;
                    }

                    result.add(fleet);
                    continue;

                }

            }
        }

        IntervalComparator comparator = battleFleet.getFleetTargetInterval().getComparator(battleFleet);
        result.sort(comparator);

        if (result.isEmpty()) {
            return null;
        }

        return result.get(0);

    }

    public boolean attackFleetByFleet(int steps, BattleFleet attacker, BattleFleet defender, FleetAttackFleetTrigger trigger) {

        // Get fleets directions
        int realAttackerDirection = attacker.getDirection();

        int attackerDirection = 2; // 2 is default attacker direction
        int defenderDirection = defender.getDirection();

        // Matrix fleet X fleet based on the default attack direction (2)
        int[][] attackerSegmentedMatrix = segmentedMatrix[attackerDirection];

        List<BattleFleetCell> defenderCells = defender.getTeam().getCells(); // defender.sortFleetByDirection(defender.getDirection());
        List<ShipPosition> sortedDefenderPositions = new ArrayList<>();

        int tempVariable = 4 - realAttackerDirection + defender.getDirection();
        int desiredDefensiveDirection = tempVariable > 3 ? tempVariable - 4 : tempVariable;

        // Matrix fleet X fleet (defensive) based on their directions
        int[][] defenderSegmentedMatrix = segmentedMatrix[desiredDefensiveDirection];

        // Set trigger directions
        trigger.setAttackerDirection(realAttackerDirection);
        trigger.setDefensiveDirection(defenderDirection);

        // Sort algorithm
        for (int defenderSegmentedPosIndex = 0; defenderSegmentedPosIndex < 3; defenderSegmentedPosIndex++) {
            for (int defenderPosIndex = 0; defenderPosIndex < 3; defenderPosIndex++) {
                int calculatedTargetPosition = defenderSegmentedMatrix[defenderSegmentedPosIndex][defenderPosIndex];
                BattleFleetCell calculatedCell = defenderCells.get(calculatedTargetPosition);
                sortedDefenderPositions.add(ShipPosition.builder()
                        .battleFleetCell(calculatedCell)
                        .direction(desiredDefensiveDirection)
                        .segmentedPosIndex(defenderSegmentedPosIndex)
                        .posIndex(defenderPosIndex)
                        .pos(calculatedTargetPosition)
                        .build());
            }
        }

        trigger.setDefensiveDirection(desiredDefensiveDirection);

        // ShipAttackCalculator
        ShipAttackCalculator calculator = new ShipAttackCalculator(this, attacker, defender, desiredDefensiveDirection, steps);
        // trigger.getShipAttackPacketActions().value(() -> BotLogger.log("ATTACKER DIR: " + attackerDirection + ", ATTACKER CUR: " + realAttackerDirection + ", DEFENDER DIR: " + defenderDirection + ", DEFENDER DESIRED: " + desiredDefensiveDirection));

        for (int attackerSegmentedPos = 0; attackerSegmentedPos < 3; attackerSegmentedPos++) {
            attackerPosLoop:
            for (int attackerPosIndex = 0; attackerPosIndex < 3; attackerPosIndex++) {

                int attackerPos = attackerSegmentedMatrix[attackerSegmentedPos][attackerPosIndex];
                BattleFleetCell attackerCell = attacker.getTeam().getCells().get(attackerPos);

                // trigger.getShipAttackPacketActions().add(() -> BotLogger.dev("(" + attackerPos + "#" + attackerCell.getShipModelId() + ") ATTACKING FROM (" + finalAttackerSegmentedPos + ", " + finalAttackerPosIndex + ")"));

                if (attackerCell.getShipModelId() < 0) {
                    continue;
                }

                ShipCellAttackMeta attackMeta = new ShipCellAttackMeta();

                attackMeta.setShipReductions(new ArrayList<>());
                attackMeta.setShipShootdowns(new ArrayList<>());
                attackMeta.setFortShootdowns(new ArrayList<>());

                attackMeta.setDefenderUsages(new ArrayList<>());
                attackMeta.setAttackerUsages(new ArrayList<>());

                attackMeta.setAttackerDirection(attacker.getDirection());
                attackMeta.setAttackerSegmentedPosIndex(attackerSegmentedPos);
                attackMeta.setAttackerPosIndex(attackerPosIndex);
                attackMeta.setAttackerPos(attackerPos);

                ShipPosition target = null;

                for (ShipPosition defenderShipPosition : sortedDefenderPositions) {

                    if (target != null && target.getPosIndex() == attackerPosIndex) {
                        break;
                    }

                    BattleFleetCell defenderCell = defenderShipPosition.getBattleFleetCell();

                    if (!defenderCell.hasShips()) {
                        continue;
                    }

                    if (target == null) {

                        target = defenderShipPosition;

                        attackMeta.setDefenderDirection(defenderShipPosition.getDirection());
                        attackMeta.setDefenderSegmentedPosIndex(defenderShipPosition.getSegmentedPosIndex());
                        attackMeta.setDefenderPosIndex(defenderShipPosition.getPosIndex());
                        attackMeta.setDefenderPos(defenderShipPosition.getPos());

                        if (attackerPosIndex == defenderShipPosition.getPosIndex()) {
                            break;
                        }

                        continue;

                    }

                    if (attackerPosIndex == defenderShipPosition.getPosIndex()) {

                        attackMeta.setDefenderDirection(defenderShipPosition.getDirection());
                        attackMeta.setDefenderSegmentedPosIndex(defenderShipPosition.getSegmentedPosIndex());
                        attackMeta.setDefenderPosIndex(defenderShipPosition.getPosIndex());
                        attackMeta.setDefenderPos(defenderShipPosition.getPos());

                        target = defenderShipPosition;
                        break;

                    } else if (defenderShipPosition.getPosIndex() < attackerPosIndex && defenderShipPosition.getSegmentedPosIndex() <= target.getSegmentedPosIndex()) {

                        attackMeta.setDefenderDirection(defenderShipPosition.getDirection());
                        attackMeta.setDefenderSegmentedPosIndex(defenderShipPosition.getSegmentedPosIndex());
                        attackMeta.setDefenderPosIndex(defenderShipPosition.getPosIndex());
                        attackMeta.setDefenderPos(defenderShipPosition.getPos());

                        target = defenderShipPosition;
                    }

                }

                if (target == null || !target.getBattleFleetCell().hasShips()) {
                    continue;
                }

                ShipPosition attackerPosition = ShipPosition.builder()
                        .battleFleetCell(attackerCell)
                        .pos(attackerPos)
                        .direction(attackerDirection)
                        .segmentedPosIndex(attackerSegmentedPos)
                        .posIndex(attackerPosIndex)
                        .build();

                calculator.calculate(attackerPosition, target, attackMeta, trigger, sortedDefenderPositions);

            }
        }

        // interrupted = true;
        return trigger.getAttacks().stream().anyMatch(ShipCellAttackMeta::isAttack);

    }

    public boolean attackFortByFleet(BattleFleet attacker, BattleFort defender, FleetAttackFortTrigger trigger) {

        // Get fleets directions
        int realAttackerDirection = attacker.getDirection();
        int attackerDirection = 2; // 2 is default attacker direction

        // Matrix fleet X fleet based on the default attack direction (2)
        int[][] attackerSegmentedMatrix = segmentedMatrix[attackerDirection];

        // ShipAttackCalculator
        ShipAttackCalculator calculator = new ShipAttackCalculator(this, attacker, defender, 0, 0);

        for (int attackerSegmentedPos = 0; attackerSegmentedPos < 3; attackerSegmentedPos++) {
            for (int attackerPosIndex = 0; attackerPosIndex < 3; attackerPosIndex++) {

                int attackerPos = attackerSegmentedMatrix[attackerSegmentedPos][attackerPosIndex];
                BattleFleetCell attackerCell = attacker.getTeam().getCells().get(attackerPos);

                if (attackerCell.getShipModelId() < 0) {
                    continue;
                }

                AssaultCellAttackMeta attackMeta = new AssaultCellAttackMeta();

                attackMeta.setFortReductions(new ArrayList<>());
                attackMeta.setShipShootdowns(new ArrayList<>());
                attackMeta.setModuleUsages(new ArrayList<>());

                attackMeta.setAttackerDirection(attacker.getDirection());
                attackMeta.setAttackerSegmentedPosIndex(attackerSegmentedPos);
                attackMeta.setAttackerPosIndex(attackerPosIndex);
                attackMeta.setAttackerPos(attackerPos);

                ShipPosition attackerPosition = ShipPosition.builder()
                        .battleFleetCell(attackerCell)
                        .pos(attackerPos)
                        .direction(attackerDirection)
                        .segmentedPosIndex(attackerSegmentedPos)
                        .posIndex(attackerPosIndex)
                        .build();

                calculator.calculate(attackerPosition, attackMeta, trigger);

            }
        }

        return trigger.getAttacks().stream().anyMatch(AssaultCellAttackMeta::isAttack);

    }

    public boolean attackFleetByFort(BattleFort attacker, BattleFleet defender, FortAttackFleetTrigger trigger) {

        List<BattleFleetCell> defenderCells = defender.getTeam().getCells();
        List<ShipPosition> sortedDefenderPositions = new ArrayList<>();

        int direction = 2;

        int tempVariable = 4 - direction + defender.getDirection();
        int desiredDefensiveDirection = tempVariable > 3 ? tempVariable - 4 : tempVariable;

        // Matrix fleet X fleet (defensive) based on their directions
        int[][] defenderSegmentedMatrix = segmentedMatrix[desiredDefensiveDirection];

        // Sort algorithm
        for (int defenderSegmentedPosIndex = 0; defenderSegmentedPosIndex < 3; defenderSegmentedPosIndex++) {
            for (int defenderPosIndex = 0; defenderPosIndex < 3; defenderPosIndex++) {
                int calculatedTargetPosition = defenderSegmentedMatrix[defenderSegmentedPosIndex][defenderPosIndex];
                BattleFleetCell calculatedCell = defenderCells.get(calculatedTargetPosition);
                sortedDefenderPositions.add(ShipPosition.builder()
                        .battleFleetCell(calculatedCell)
                        .direction(desiredDefensiveDirection)
                        .segmentedPosIndex(defenderSegmentedPosIndex)
                        .posIndex(defenderPosIndex)
                        .pos(calculatedTargetPosition)
                        .build());
            }
        }

        // FortAttackCalculator
        FortAttackCalculator calculator = new FortAttackCalculator(this, attacker, defender);
        calculator.calculate(attacker.getDamage(), sortedDefenderPositions, trigger);

        return trigger.getAttacks().stream().anyMatch(FortCellAttackMeta::isAttack);

    }

    public BattleFort getCurrentFort(int fortId) {

        for (BattleFort battleFort : match.getFortsSorted()) {
            if (battleFort.getFortId() == fortId) {
                return battleFort;
            }
        }

        return null;

    }

    public BattleFleet getCurrentFleet(int shipTeamId) {

        for (BattleFleet battleFleet : match.getFleetsSorted()) {
            if (battleFleet.getShipTeamId() == shipTeamId) {
                return battleFleet;
            }
        }

        return null;

    }

    public BattleFort getRoundFort(int fortId) {

        for (BattleFort battleFort : match.getCurrentRound().getForts()) {
            if (battleFort.getFortId() == fortId) {
                return battleFort;
            }
        }

        return null;

    }

    public BattleFleet getRoundFleet(int shipTeamId) {

        for (BattleFleet battleFleet : match.getCurrentRound().getFleets()) {
            if (battleFleet.getShipTeamId() == shipTeamId) {
                return battleFleet;
            }
        }

        return null;

    }

    public int getRound() {

        return getMatch().getRound();
    }

    public BattleCell[][] getCells(List<BattleElement> temporalElements) {

        BattleCell[][] cells = new BattleCell[25][25];

        for (BattleElement element : temporalElements) {

            if (cells[element.getPosX()][element.getPosY()] == null) {
                cells[element.getPosX()][element.getPosY()] = new BattleCell();
            }

            BattleCell cell = cells[element.getPosX()][element.getPosY()];

            if (element.getType() == BattleElementType.FLEET) {
                cell.addFleet((BattleFleet) element);
            } else {

                BattleFort battleFort = (BattleFort) element;

                if (battleFort.getRadius() == 2) {

                    int baseX = battleFort.getPosX();
                    int baseY = battleFort.getPosY();

                    addBigFort(cells, battleFort, baseX, baseY - 1);
                    addBigFort(cells, battleFort, baseX + 1, baseY - 1);
                    addBigFort(cells, battleFort, baseX + 1, baseY);

                }

                cell.addFort((BattleFort) element);

            }

        }

        return cells;

    }

    private void addBigFort(BattleCell[][] cells, BattleFort fort, int posX, int posY) {

        if (cells[posX][posY] == null) {
            cells[posX][posY] = new BattleCell();
        }
        BattleCell cell = cells[posX][posY];
        cell.addFort(fort);

    }

}

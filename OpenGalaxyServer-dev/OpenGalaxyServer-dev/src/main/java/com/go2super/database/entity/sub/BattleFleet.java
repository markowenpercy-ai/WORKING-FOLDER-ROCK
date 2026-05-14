package com.go2super.database.entity.sub;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.BattleElementType;
import com.go2super.database.entity.type.MatchType;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.resources.data.meta.BuildEffectMeta;
import com.go2super.service.PacketService;
import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.BattleFleetTeam;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.calculator.FleetEffect;
import com.go2super.service.battle.calculator.FleetEffects;
import com.go2super.service.battle.calculator.ShipProc;
import com.go2super.service.battle.calculator.ShipTechs;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.service.battle.type.EffectType;
import com.go2super.service.battle.type.Target;
import com.go2super.service.battle.type.TargetInterval;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Data
public class BattleFleet extends BattleElement implements Comparable<BattleFleet>, Serializable {

    @Transient
    public static final int[][] attackMatrix = {
            {6, 7, 8, 3, 4, 5, 0, 1, 2},
            {0, 3, 6, 1, 4, 7, 2, 5, 8},
            {0, 1, 2, 3, 4, 5, 6, 7, 8},
            {8, 5, 2, 7, 4, 1, 6, 3, 0}
    };

    private BattleCommander battleCommander;
    private String name;

    private int guid;
    private int shipTeamId;

    private double he3;
    private double maxHe3;

    private int bodyId;

    // private BattleElementType targetType = BattleElementType.FLEET;
    // private int targetId = -1;

    private int target;
    private int targetInterval;

    private int posX;
    private int posY;

    private int direction;
    private int maxRounds;
    private int joinRound;

    private long roundAttack;
    private long roundDurability;

    private int movement;

    private BattleFleetTeam team;
    private boolean defender;

    private ShipProc proc;
    private ShipTechs techs;
    private String otherSkill;

    private boolean animation = false;
    private FleetEffects effects = new FleetEffects();

    public BattleFleet() {

        super(BattleElementType.FLEET);
    }

    private Fleet getRealFleet() {

        return PacketService.getInstance().getFleetCache().findByShipTeamId(shipTeamId);
    }

    public void update(Match match) {

        if (match.getMatchType().isVirtual()) {
            return;
        }
        // if(guid == -1) return;

        Fleet fleet = getRealFleet();
        if (fleet == null) {
            return;
        }

        fleet.setPosX(getPosX());
        fleet.setPosY(getPosY());
        fleet.setDirection(getDirection());

        fleet.setHe3((int) getHe3());

        if (fleet.ships() <= 0) {
            fleet.remove();
        } else {
            fleet.save();
        }

        User user = fleet.getUser();
        if (user == null) {
            return;
        }

        double shipRepairRate = 0.0d;
        UserBuilding spaceDock = user.getBuildings().getBuilding("build:shipRepair");

        if (spaceDock != null) {

            BuildEffectMeta repairEffect = spaceDock.getLevelData().getEffect("shipRepair");
            if (repairEffect != null) {
                shipRepairRate = repairEffect.getValue();
            }

        }
        boolean repairable = MatchType.getAllNonVirtual().contains(match.getMatchType());
        UserShips userShips = user.getShips();
        Commander commander = fleet.getCommander();

        if (fleet.ships() <= 0) {

            if (repairable) {

                ShipTeamBody shipTeamBody = fleet.getFleetBody();

                for (ShipTeamNum teamNum : shipTeamBody.getCells()) {

                    if (teamNum.getShipModelId() == -1 || teamNum.getNum() <= 0) {
                        continue;
                    }

                    int repairNum = (int) (teamNum.getNum() * shipRepairRate);
                    if (repairNum <= 0) {
                        continue;
                    }

                    userShips.addRepair(teamNum.getShipModelId(), repairNum);

                }

            }

            if (match.getMatchType() != MatchType.ARENA_MATCH && match.getMatchType() != MatchType.LEAGUE_MATCH) {
                if (commander.isAngla()) {
                    commander.setUntilRest(DateUtil.now(172800)); // 2 days
                } else {

                    boolean kill = MathUtil.random(1, 100) >= 50;
                    commander.setDead(kill);
                    commander.setInjuredMatch(match.getId());

                    if (!kill) {
                        commander.setUntilRest(DateUtil.now(345600)); // 4 days
                    }
                }
            }
            commander.save();

        } else {

            commander.save();

        }

        ShipTeamBody shipTeamBody = fleet.getFleetBody();
        int index = 0;

        for (int i = 0; i < 9; i++) {

            BattleFleetCell battleFleetCell = getCell(i);

            ShipTeamNum oldNum = shipTeamBody.getCells().get(i);
            ShipTeamNum teamNum = new ShipTeamNum();

            teamNum.setShipModelId(battleFleetCell.getShipModelId());
            teamNum.setNum(battleFleetCell.getAmount());

            if (repairable) {
                int repairNum = (int) ((oldNum.getNum() - teamNum.getNum()) * shipRepairRate);
                if (repairNum > 0) {
                    userShips.addRepair(oldNum.getShipModelId(), repairNum);
                }
            }

            shipTeamBody.cells.set(index++, teamNum);

        }


        if (isDestroyed()) {
            if (match.getMatchType() != MatchType.ARENA_MATCH && match.getMatchType() != MatchType.LEAGUE_MATCH) {
                if (commander.isAngla()) {
                    commander.setUntilRest(DateUtil.now(172800)); // 2 days
                } else {

                    boolean kill = MathUtil.random(1, 100) >= 50;
                    commander.setDead(kill);
                    commander.setInjuredMatch(match.getId());

                    if (!kill) {
                        commander.setUntilRest(DateUtil.now(345600)); // 4 days
                    }

                }
            }
            commander.save();
            fleet.remove();
        }

    }

    public void calculate() {

        calculateDurability();
        calculateAttack();
        calculateMovement();
    }

    public void reset() {

        roundAttack = 0;
        roundDurability = 0;
    }

    public void recalculate() {
        roundAttack = 0;
        roundDurability = 0;
        calculate();
    }

    public void process(FleetEffect fleetEffect) {

        Optional<FleetEffect> optionalEffect = effects.getEffects().stream().filter(effect -> effect.getEffectType() == fleetEffect.getEffectType()).findFirst();

        if (optionalEffect.isPresent()) {

            FleetEffect effect = optionalEffect.get();
            effect.setValue(effect.getValue() + fleetEffect.getValue());
            effect.setUntil(fleetEffect.getUntil());

            if (effect.getValue() == 0 || fleetEffect.getValue() == 0) {
                effects.getEffects().remove(effect);
            }
            return;

        }

        if (fleetEffect.getValue() == 0) {
            return;
        }
        effects.getEffects().add(fleetEffect);

    }

    public boolean isNullified() {

        return getEffects().contains(EffectType.REGGIE) || getEffects().contains(EffectType.CIRCE);
    }

    public boolean isCommanded(String commanderNameId) {

        if (isNullified()) {
            return false;
        }
        return battleCommander.getNameId().equals(commanderNameId) || commanderNameId.equals(otherSkill);
    }

    public boolean trigger(String commanderNameId) {

        if (!battleCommander.getNameId().equals(commanderNameId) && !commanderNameId.equals(otherSkill)) {
            return false;
        }

        double rate = battleCommander.getTrigger().getRate();
        double random = MathUtil.random(1, 100);
        return random <= rate;

    }

    public boolean trigger(String commanderNameId, int round) {

        if (!battleCommander.getNameId().equals(commanderNameId)) {
            return false;
        }

        if (proc == null || proc.getRound() != round) {

            double rate = battleCommander.getTrigger().getRate();

            proc = new ShipProc();
            proc.setRate(rate * 0.01);
            proc.setRound(round);

            proc.setTriggered(MathUtil.random(1, 100) <= rate);
            return proc.isTriggered();

        }

        return proc.isTriggered();

    }

    public List<BattleFleetCell> sortFleetByDirection(int desiredDirection) {

        List<BattleFleetCell> result = new ArrayList<>(team.getCells());

        for (int positionIndex = 0; positionIndex < 9; positionIndex++) {

            int currentPosition = attackMatrix[direction][positionIndex];
            int desiredPosition = attackMatrix[desiredDirection][positionIndex];

            result.set(desiredPosition, team.getCells().get(currentPosition));

        }

        return result;

    }

    public int getAmount() {

        int amount = 0;

        for (BattleFleetCell cell : team.getCells()) {
            if (cell.hasShips()) {
                amount += cell.getAmount();
            }
        }

        return amount;

    }

    public long calculateDurability() {

        if (roundDurability != 0) {
            return roundDurability;
        }

        long totalShields = 0;
        long totalStructure = 0;

        long additionalShield = (long) battleCommander.getAdditionalShield();
        long additionalArmor = (long) battleCommander.getAdditionalArmor();

        for (BattleFleetCell cell : team.getCells()) {
            if (cell.hasShips()) {
                ShipModel shipModel = PacketService.getShipModel(cell.getShipModelId());
                if (shipModel == null) {
                    continue;
                }
                long shields = (long) shipModel.getShields() * cell.getAmount() + additionalShield;
                long structure = (long) shipModel.getStructure() * cell.getAmount() + additionalArmor;

                totalStructure += structure;
                totalShields += shields;
            }
        }

        totalShields = (long) (totalShields * (1 + battleCommander.getShieldIncrement()));
        totalStructure = (long) (totalStructure * (1 + battleCommander.getStructureIncrement()));

        roundDurability = totalStructure + totalShields;
        return roundDurability;

    }

    public long calculateAttack() {

        if (roundAttack != 0) {
            return roundAttack;
        }

        List<Pair<String, Double>> additionalAttack = battleCommander.getAdditionalMaxAttackWithoutPlanetary();
        for (BattleFleetCell cell : team.getCells()) {
            if (cell.hasShips()) {
                ShipModel model = PacketService.getShipModel(cell.getShipModelId());
                if (model == null) {
                    continue;
                }
                roundAttack += ((long) (model.getMaxAttack(additionalAttack)) * (long) cell.getAmount());
            }
        }

        roundAttack *= (long) (1 + battleCommander.getAttackPowerIncrement());
        return roundAttack;

    }

    public int calculateMovement() {

        int result = Integer.MAX_VALUE;

        for (BattleFleetCell cell : team.getCells()) {
            if (cell.hasShips()) {

                int cache = cell.getMovement();

                if (cache < result) {
                    result = cache;
                }

            }
        }

        if (result == Integer.MAX_VALUE) {
            result = 0;
        }

        movement = Math.min(result, 16);
        return movement;


    }

    public boolean canAttack(BattleElement target) {

        Node from = getNode();
        Node to = target.getNode();

        int distance = from.getHeuristic(to);
        return distance >= getMinRange() && distance <= getMaxRange();

    }

    public void setDirection(int direction) {

        this.direction = direction;

    }

    public boolean isDefender() {

        return defender;
    }

    public boolean isAttacker() {

        return !defender;
    }

    public boolean isEnemy(BattleFleet fleet) {

        if (fleet.isAttacker() && isAttacker()) {
            return false;
        }
        return !fleet.isDefender() || !isDefender();
    }

    public int getTeamId() {

        return isAttacker() ? 1 : 0;
    }

    public int ships() {

        int number = 0;
        for (BattleFleetCell cell : team.getCells()) {
            if (cell.getShipModelId() > -1 && cell.getAmount() > 0) {
                number += cell.getAmount();
            }
        }
        return number;
    }

    public int getMinRange() {

        int result = 0;

        for (BattleFleetCell cell : team.getCells()) {
            if (cell.getShipModelId() >= -1 && cell.getAmount() > 0) {

                List<BattleFleetAttackModule> weapons = cell.getWeaponModules(true);

                for (BattleFleetAttackModule fleetAttackModule : weapons) {
                    if (result == 0) {
                        result = fleetAttackModule.getMinRange();
                    } else if (result > fleetAttackModule.getMinRange()) {
                        result = fleetAttackModule.getMinRange();
                    }
                }

            }
        }

        return result;

    }

    public int getMaxRange() {

        int result = 0;

        for (BattleFleetCell cell : team.getCells()) {
            if (cell.getShipModelId() >= -1 && cell.getAmount() > 0) {

                List<BattleFleetAttackModule> weapons = cell.getWeaponModules(true);

                for (BattleFleetAttackModule fleetAttackModule : weapons) {
                    int maxRange = fleetAttackModule.getMaxRange(techs);
                    if (result == 0) {
                        result = maxRange;
                    } else if (result < maxRange) {
                        result = maxRange;
                    }
                }

            }
        }

        return result;

    }

    public int getTotalDurability() {

        int durability = 0;

        for (BattleFleetCell cell : getCells()) {
            if (cell.hasShips()) {
                durability += (cell.getShields() + cell.getStructure());
            }
        }

        return durability;

    }

    public int getTotalStructure() {

        int structure = 0;
        for (BattleFleetCell cell : getCells()) {
            if (cell.hasShips()) {
                structure += cell.getStructure();
            }
        }
        return structure;
    }

    public int getTotalShields() {

        int shields = 0;
        for (BattleFleetCell cell : getCells()) {
            if (cell.hasShips()) {
                shields += cell.getShields();
            }
        }
        return shields;
    }


    public Target getFleetTarget() {

        return target == 0 ? Target.MIN_RANGE : Target.MAX_RANGE;
    }

    public TargetInterval getFleetTargetInterval() {

        return Arrays.asList(TargetInterval.values()).get(targetInterval);
    }

    public BattleFleetCell getCell(int position) {

        return getTeam().getCells().get(position);
    }

    public List<BattleFleetCell> getCells() {

        return getTeam().getCells();
    }

    public boolean isDestroyed() {

        for (BattleFleetCell cell : getCells()) {
            if (cell.hasShips()) {
                return false;
            }
        }
        return true;
    }

    public boolean isPirate() {

        return guid == -1;
    }

    public boolean hasRocky() {

        int skill = battleCommander.getSkillId();
        return skill == 82 || skill == 53;
    }

    public boolean hasRobert() {

        int skill = battleCommander.getSkillId();
        return skill == 78 || skill == 68 || skill == 45;
    }

    public boolean canAffect(BattleFleet other) {

        if (other.isCommanded("commander:rayo")
                || other.isCommanded("commander:dictators")
                || other.isCommanded("commander:deathFromAbove")
                || other.isCommanded("commander:rexScuta")
        ) {
            return battleCommander.isTotalMoreThan(other.getBattleCommander());
        }
        return true;
    }

    public Optional<ShipModel> getFlagship() {

        for (BattleFleetCell cell : getCells()) {
            if (cell.hasShips()) {
                ShipModel model = PacketService.getShipModel(cell.getShipModelId());
                if (model.isFlagship()) {
                    return Optional.of(model);
                }
            }
        }
        return Optional.empty();
    }

    public Double getEvaAttackRows(){
        var fleetEffect = getEffects().getEffect(EffectType.Eva);
        if(fleetEffect != null){
            return fleetEffect.getValue();
        }
        else{
            return null;
        }
    }

    @Override
    public int compareTo(BattleFleet fleet) {

        if (getBattleCommander().getTotalSpeed() > fleet.getBattleCommander().getTotalSpeed()) {
            return -1;
        } else if (getBattleCommander().getTotalSpeed() < fleet.getBattleCommander().getTotalSpeed()) {
            return 1;
        } else {
            return 0;
        }
    }

}

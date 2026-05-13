package com.go2super.service.battle.match;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.ShipTeamBody;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.InstanceType;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.instance.ResponseEctypePassPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.packet.ship.ResponseGalaxyShipPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.PropData;
import com.go2super.resources.data.meta.BuildEffectMeta;
import com.go2super.resources.data.meta.RewardMeta;
import com.go2super.resources.data.meta.BonusRewardMeta;
import com.go2super.service.*;
import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.StopCause;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

@Getter
@Setter
@ToString(callSuper = true)
public class InstanceMatch extends Match {

    private int guid;

    private InstanceType type;
    private InstanceData data;

    private List<BattleTag> tags = new ArrayList<>();
    private List<BattleRound> rounds = new ArrayList<>();

    private List<BattleFleet> removedFleets = new ArrayList<>();
    private List<BattleFort> removedForts = new ArrayList<>();

    public boolean hasWon() {

        return getFleets().stream().anyMatch(fleet -> fleet.getGuid() == guid) && !hasPirate() && !hasForts();
    }

    public boolean hasPirate() {

        return getFleets().stream().anyMatch(fleet -> fleet.isPirate());
    }

    public boolean hasForts() {

        return !getForts().isEmpty();
    }

    public InstanceMatch(InstanceData data) {

        this.data = data;
    }

    @Override
    public void stop(StopCause cause) {

        returnAllFleets();

        User user = UserService.getInstance().getUserCache().findByGuid(guid);
        Optional<LoggedGameUser> optionalGameUser = LoginService.getInstance().getGame(guid);

        boolean won = (cause == StopCause.AUTOMATIC && hasWon());

        if (won) {

            List<Pair<PropData, Integer>> rewards = new ArrayList<>();

            switch (getType()) {

                case RESTRICTED:

                    for (RewardMeta rewardMeta : data.getRewards()) {
                        if (rewardMeta.getWeight() >= 100) {

                            PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                            if (defResult == null) {
                                continue;
                            }

                            rewards.add(Pair.of(defResult, rewardMeta.getNum()));

                        } else {

                            int random = MathUtil.randomInclusive(1, 100);
                            if (random <= rewardMeta.getWeight()) {

                                PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                                if (defResult == null) {
                                    continue;
                                }

                                rewards.add(Pair.of(defResult, rewardMeta.getNum()));

                            }

                        }
                    }

                    break;
                case TRIALS:
                    for (var set : data.getRewards().stream().collect((Collectors.groupingBy(RewardMeta::getGroup))).entrySet()) {
                        double totalWeight = 0.0;
                        for (var reward: set.getValue()) {
                            totalWeight += reward.getWeight();
                        }
                        int idx = 0;
                        for (double r = Math.random() * totalWeight; idx < set.getValue().size() - 1; ++idx) {
                            var item = set.getValue().get(idx);
                            r -= item.getWeight();
                            if (r <= 0.0) break;
                        }
                        var item = set.getValue().get(idx);
                        rewards.add(Pair.of(ResourceManager.getProps().getData(item.getProp()), item.getNum() ));
                    }
                    break;
                case CONSTELLATION:
                    for (var set : data.getRewards().stream().collect((Collectors.groupingBy(RewardMeta::getGroup))).entrySet()) {
                        if(!Objects.equals(set.getKey(), "Default")){
                            int random = MathUtil.randomInclusive(1, 100);
                            if(random > 50){
                                continue;
                            }
                        }
                        double totalWeight = 0.0;
                        for (var reward: set.getValue()) {
                            totalWeight += reward.getWeight();
                        }
                        int idx = 0;
                        for (double r = Math.random() * totalWeight; idx < set.getValue().size() - 1; ++idx) {
                            var item = set.getValue().get(idx);
                            r -= item.getWeight();
                            if (r <= 0.0) break;
                        }
                        var item = set.getValue().get(idx);
                        rewards.add(Pair.of(ResourceManager.getProps().getData(item.getProp()), item.getNum() ));
                    }
                    break;
                default:
                    RewardMeta rewardMeta = data.pickOne();

                    PropData defResult = ResourceManager.getProps().getData(rewardMeta.getProp());
                    if (defResult == null) {
                        break;
                    }

                    rewards.add(Pair.of(defResult, rewardMeta.getNum()));
                    break;

            }

            // Bonus reward roll (optional per-instance)
            BonusRewardMeta bonus = getData().getBonusReward();
            if (bonus != null && bonus.getChance() > 0) {
                int roll = MathUtil.randomInclusive(1, 100);
                if (roll <= bonus.getChance()) {
                    PropData bonusProp = ResourceManager.getProps().getData(bonus.getProp());
                    if (bonusProp != null) {
                        rewards.add(Pair.of(bonusProp, 1));
                    }
                }
            }

            user.getMetrics().add(getData().getName(), 1);

            List<Packet> sendOnlinePackets = new ArrayList<>();

            int instanceId = getData().getId();
            if (getType() == InstanceType.INSTANCE && user.getStats().getInstance() < (instanceId + 1)) {

                user.getStats().setInstance(instanceId + 1);

                ResponseEctypePassPacket packet = new ResponseEctypePassPacket();

                packet.setDataLen(user.getStats().getInstance());
                packet.fill(user.getStats().getInstance());

                sendOnlinePackets.add(packet);

            }

            if (getType() == InstanceType.TRIALS) {
                if(new Date(getStartDate()).getDay() == new Date().getDay()){
                    //only on the same day should step forward, if it goes to the next day should reset back to Trial 1
                    user.getStats().setTrial(((instanceId - 62) + 11));
                    sendOnlinePackets.add(ResourcesService.getInstance().getPlayerResourcePacket(user));
                }
            }

            //
            // Give EXP to the commanders
            //
            if (data.getExperienceGain() > 0) {
                for (BattleFleet battleFleet : getAllFleets(guid)) {

                    Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(battleFleet.getShipTeamId());

                    if (fleet == null || fleet.getGuid() != battleFleet.getGuid()) {
                        continue;
                    }

                    Commander commander = fleet.getCommander();
                    if (commander == null) {
                        continue;
                    }

                    int gainedExp = commander.addExp((int) (data.getExperienceGain() * (1 + commander.getStoneExp())));
                    commander.save();

                    BattleExpCache expCache = new BattleExpCache();

                    expCache.setGuid(guid);
                    expCache.setCommanderId(commander.getCommanderId());
                    expCache.setLevelId(CommanderService.getInstance().getLevel(commander).getLevel());
                    expCache.setExp(gainedExp);
                    expCache.setHeadId(commander.getSkill());

                    getBattleReport().getExpHistoric().add(expCache);

                }
            }

            if (rewards != null && !rewards.isEmpty()) {

                //
                // Send instance chest
                // to the email
                //
                UserEmailStorage userEmailStorage = user.getUserEmailStorage();

                Email email = Email.builder()
                    .autoId(userEmailStorage.nextAutoId())
                    .name("System")
                    .readFlag(0)
                    .date(DateUtil.now())
                    .goods(new ArrayList<>())
                    .guid(-1)
                    .build();

                if (getType() == InstanceType.INSTANCE) {

                    email.setSubject("Instance Rewards");
                    email.setEmailContent("You completed the instance " + (instanceId + 1) + " and earned the following rewards:");

                } else if (getType() == InstanceType.RESTRICTED) {

                    email.setSubject("Restricted Instance Rewards");
                    email.setEmailContent("You completed the restricted instance " + ((instanceId - 39) + 10) + " and earned the following rewards:");

                } else if (getType() == InstanceType.TRIALS) {

                    email.setSubject("Trial Rewards");
                    email.setEmailContent("You completed the trial " + ((instanceId - 62) + 11) + " and earned the following rewards:");

                } else if (getType() == InstanceType.CONSTELLATION) {

                    email.setSubject("Constellation Instance Rewards");
                    email.setEmailContent("You completed the constellation " + data.getName() + " and earned the following rewards:");

                }

                email.setType(2);

                for (Pair<PropData, Integer> reward : rewards) {
                    email.addGood(EmailGood.builder()
                        .goodId(reward.getKey().getId())
                        .lockNum(reward.getValue())
                        .build());
                }

                userEmailStorage.addEmail(email);

                ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();
                sendOnlinePackets.add(packet);

            }

            Optional<LoggedGameUser> gameUserOptional = LoginService.getInstance().getGame(user);

            if (gameUserOptional.isPresent()) {

                LoggedGameUser loggedGameUser = gameUserOptional.get();
                loggedGameUser.getSmartServer().send(sendOnlinePackets);

            }

        }

        user.update();
        user.save();

        if (optionalGameUser.isPresent()) {

            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();

            response.setEctypeId((short) 0);
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 0);

            optionalGameUser.get().getSmartServer().send(response);
            optionalGameUser.get().getSmartServer().send(CommanderService.getInstance().getRefreshBaseInfoPacket(user));

        }

        if (cause == StopCause.AUTOMATIC && optionalGameUser.isPresent()) {

            ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getInstanceFightResult(this, won);
            sendPacketToViewers(fightResultPacket);
            if (getId().equals(optionalGameUser.get().getMatchViewing())) {
                optionalGameUser.get().setMatchViewing(null);
            }

        }

    }

    @Override
    public void updateFleet(BattleFleet battleFleet) {

        if (battleFleet.getGuid() == -1) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(battleFleet.getShipTeamId());
        if (fleet == null) {
            return;
        }
        if (fleet.getGuid() == -1) {
            return;
        }

        fleet.setHe3((int) Math.min(0, battleFleet.getHe3()));
        fleet.save();

    }

    @Override
    public void removeFleet(BattleFleet battleFleet) {

        getFleets().remove(battleFleet);
        removedFleets.add(battleFleet);

    }

    @Override
    public void removeFort(BattleFort battleFort) {

        getForts().remove(battleFort);
        removedForts.add(battleFort);

    }

    @Override
    public void returnAllFleets() {

        User user = UserService.getInstance().getUserCache().findByGuid(guid);
        UserPlanet userPlanet = user.getPlanet();

        for (BattleFleet pirateFleet : getAllFleets(-1)) {

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(pirateFleet.getShipTeamId());
            if (fleet == null) {
                continue;
            }

            if (fleet.getGuid() != guid) {
                PacketService.getInstance().getFleetCache().delete(fleet);
                continue;
            }

        }

        List<Fleet> fleets = new ArrayList<>();
        List<BattleFleet> battleFleets = getAllFleets(guid);

        double shipRepairRate = 0.0d;
        UserBuilding spaceDock = user.getBuildings().getBuilding("build:shipRepair");

        if (spaceDock != null) {

            BuildEffectMeta repairEffect = spaceDock.getLevelData().getEffect("shipRepair");
            if (repairEffect != null) {
                shipRepairRate = repairEffect.getValue();
            }

        }

        UserShips userShips = user.getShips();

        if (getType() == InstanceType.INSTANCE || getType() == InstanceType.RESTRICTED) {

            for (BattleFleet userFleet : battleFleets) {

                Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(userFleet.getShipTeamId());
                if (fleet == null) {
                    continue;
                }

                Commander commander = fleet.getCommander();

                if (userFleet.ships() <= 0) {
                    fleet.remove();
                    if (commander.isAngla()) {
                        commander.setUntilRest(DateUtil.now(172800)); // 2 days
                    } else {

                        boolean kill = MathUtil.random(1, 100) >= 50;
                        commander.setDead(kill);

                        if (!kill) {
                            commander.setUntilRest(DateUtil.now(345600)); // 4 days
                        }

                    }

                    commander.save();
                    continue;

                } else {

                    commander.save();

                }

                ShipTeamBody shipTeamBody = fleet.getFleetBody();
                int index = 0;

                for (int i = 0; i < 9; i++) {

                    BattleFleetCell battleFleetCell = userFleet.getCell(i);
                    ShipTeamNum teamNum = new ShipTeamNum();

                    teamNum.setShipModelId(battleFleetCell.getShipModelId());
                    teamNum.setNum(battleFleetCell.getAmount());

                    shipTeamBody.cells.set(index++, teamNum);

                }

                fleet.setMatch(false);
                fleet.setFleetMatch(null);
                fleet.setHe3((int) userFleet.getHe3());
                fleet.setGalaxyId(userPlanet.getPosition().galaxyId());
                fleet.save();

                fleets.add(fleet);

            }

        } else {

            for (BattleFleet userFleet : battleFleets) {

                Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(userFleet.getShipTeamId());
                if (fleet == null) {
                    continue;
                }

                Commander commander = fleet.getCommander();
                if (commander != null) {
                    commander.save();
                }

                fleet.setMatch(false);
                fleet.setFleetMatch(null);
                fleet.setHe3((int) userFleet.getHe3());
                fleet.setGalaxyId(userPlanet.getPosition().galaxyId());
                fleet.save();

                fleets.add(fleet);

            }

        }

        user.update();
        user.save();

        for (LoggedGameUser gameUser : LoginService.getInstance().getPlanetViewers(userPlanet.getPosition().galaxyId())) {

            ResponseGalaxyShipPacket galaxyShipPackets = GalaxyService.getInstance().getGalaxyShipInfo(gameUser.getGuid(), userPlanet.getPosition().galaxyId(), fleets);
            gameUser.getSmartServer().send(galaxyShipPackets);

        }

    }

    @Override
    public AttackSideType fortressAttackType() {

        return AttackSideType.DEFENDER;
    }

    @Override
    public boolean hasUser(int guid) {

        return this.guid == guid;
    }

    public List<BattleFleet> getAllFleets(int guid) {

        List<BattleFleet> aliveFleets = getFleets().stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());
        List<BattleFleet> removedFleets = getRemovedFleets().stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());

        return Stream.concat(aliveFleets.stream(), removedFleets.stream()).collect(Collectors.toList());

    }

}

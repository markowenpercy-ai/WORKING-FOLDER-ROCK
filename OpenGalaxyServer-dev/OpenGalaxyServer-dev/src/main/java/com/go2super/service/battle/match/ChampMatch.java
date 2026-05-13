package com.go2super.service.battle.match;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.logger.BotLogger;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.fight.ResponseFightResultPacket;
import com.go2super.packet.fight.ResponseWarfieldStatusPacket;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.mail.ResponseNewEmailNoticePacket;
import com.go2super.resources.ResourceManager;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.BattleResultType;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.champ.ChampPhase;
import com.go2super.service.champ.ChampService;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
@Setter
@ToString(callSuper = true)
public class ChampMatch extends Match {

    private List<Integer> sourceIds = new ArrayList<>();
    private List<Integer> targetIds = new ArrayList<>();
    @Getter
    private ChampPhase.Phase phase;
    private int roomId = -1;
    private int roomMaskId = -1;

    private int sourceSend = 0;
    private int targetSend = 0;

    private List<BattleTag> tags = new ArrayList<>();
    private List<BattleRound> rounds = new ArrayList<>();

    private List<BattleFleet> removedFleets = new ArrayList<>();

    public ChampMatch() {
    }


    @Override
    public void stop(StopCause cause) {
        BotLogger.info("ChampMatch: Stopping champ match");
        returnAllFleets();

        if (StopCause.MANUAL.equals(cause)) {
            sourceIds.clear();
            targetIds.clear();
            getFleets().clear();
            removedFleets.clear();
            rounds.clear();
            tags.clear();
            return;
        }

        BattleResultType resultType;
        if (hasWon()) {
            resultType = BattleResultType.WIN;
        } else if (hasLost()) {
            resultType = BattleResultType.LOSE;
        } else {
            resultType = BattleResultType.DRAW;
        }

        ResponseFightResultPacket fightResultPacket = BattleService.getInstance().getChampFightResult(this, resultType);

        Set<Integer> allIds = new HashSet<>();
        allIds.addAll(getSourceIds());
        allIds.addAll(getTargetIds());
        allIds.addAll(getViewers().stream().map(LoggedGameUser::getGuid).toList());
        for (Integer uid : allIds) {
            Optional<LoggedGameUser> optionalOwner = LoginService.getInstance().getGame(uid);
            optionalOwner.ifPresent(online -> {
                User updatedUser = online.getUpdatedUser();
                online.setViewing(updatedUser.getGalaxyId());
                online.setMatchViewing(null);
                ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
                response.setEctypeId((short) 1002);
                response.setGateId(UnsignedChar.of(0));
                response.setState((byte) 0);

                ResponseWarfieldStatusPacket response2 = new ResponseWarfieldStatusPacket();
                response2.setWarfield(ChampService.getInstance().getCurrentWarFieldBitMask());
                response2.setUserNumber(UnsignedShort.of(0));
                response2.setStatus((byte) -2);
                response2.setMatchLevel((byte) updatedUser.getCurrentLeague());
                online.getSmartServer().send(response, response2, fightResultPacket);
            });
        }


        BattleReport battleReport = getBattleReport();
        Map<Integer, Integer> shootdownsOverall = new HashMap<>();

        for (BattleShipCache shipCache : battleReport.getShipHistoric()) {
            if (shootdownsOverall.containsKey(shipCache.getGuid())) {
                shootdownsOverall.put(shipCache.getGuid(), (int) (shootdownsOverall.get(shipCache.getGuid()) + shipCache.getShootdowns()));
            } else {
                shootdownsOverall.put(shipCache.getGuid(), (int) shipCache.getShootdowns());
            }

        }

        shootdownsOverall = shootdownsOverall.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        int idx = 0;
        for (Map.Entry<Integer, Integer> entry : shootdownsOverall.entrySet()) {
            User user = UserService.getInstance().getUserCache().findByGuid(entry.getKey());
            if (user == null) {
                continue;
            }
            int points = 0;

            boolean attacker = getAllFleets().stream().anyMatch(fleet -> fleet.getGuid() == user.getGuid() && fleet.isAttacker());
            boolean win = (resultType == BattleResultType.LOSE && attacker) || (resultType == BattleResultType.WIN && !attacker);
            if (win) {
                int winPoints = 300;
                points += winPoints;
            }
            if (entry.getValue() > 0) {
                int rewardsByPlace = getRewardsByPlace(idx + 1);
                points += rewardsByPlace;
            }

            if (points > 0) {
                user.getResources().setChampionPoints(user.getResources().getChampionPoints() + points);

                UserChampStats stats = user.getStats().getUserChampStats();
                stats.setWins(stats.getWins() + (win ? 1 : 0));
                stats.setShootdowns(stats.getShootdowns() + entry.getValue());
                stats.setPoints(stats.getPoints() + points);

                user.update();
                user.save();
            }

            Optional<LoggedGameUser> onlineUser = user.getLoggedGameUser();
            UserEmailStorage userEmailStorage = user.getUserEmailStorage();
            String content = String.format("You have placed %d in the championship.\nYou earned %d champion points.\nYour shootdowns: %d", idx + 1, points, entry.getValue());
            Email battleMail = Email.builder()
                    .autoId(userEmailStorage.nextAutoId())
                    .type(2)
                    .name("System")
                    .subject("Champion Rewards")
                    .emailContent(content)
                    .readFlag(0)
                    .date(DateUtil.now())
                    .goods(new ArrayList<>())
                    .guid(-1)
                    .build();

            if (ChampPhase.Phase.QUALIFICATION == phase && (idx == 0 || idx == 1 || idx == 2)) {
                battleMail.addGood(EmailGood.builder()
                        .goodId(ResourceManager.getProp("prop:other.championsPass").getId())
                        .lockNum(1)
                        .build());
            }

            userEmailStorage.addEmail(battleMail);
            ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();
            onlineUser.ifPresent(online -> online.getSmartServer().send(packet));
            idx += 1;
        }

        List<Integer> allParticipants = new ArrayList<>();
        allParticipants.addAll(getSourceIds());
        allParticipants.addAll(getTargetIds());
        for (Integer x : allParticipants) {
            if (shootdownsOverall.containsKey(x)) {
                continue;
            }
            User user = UserService.getInstance().getUserCache().findByGuid(x);
            UserEmailStorage storage = user.getUserEmailStorage();
            Email battleMail = Email.builder()
                    .autoId(storage.nextAutoId())
                    .type(2)
                    .name("System")
                    .subject("Champion Rewards")
                    .emailContent("You did not get a single shootdown this time, so no rewards :/")
                    .readFlag(0)
                    .date(DateUtil.now())
                    .goods(new ArrayList<>())
                    .guid(-1)
                    .build();
            storage.addEmail(battleMail);
            ResponseNewEmailNoticePacket packet = ResponseNewEmailNoticePacket.builder()
                    .errorCode(0)
                    .build();
            user.getLoggedGameUser().ifPresent(online -> online.getSmartServer().send(packet));
        }
    }


    public boolean hasWon() {
        return getFleets().stream().noneMatch(BattleFleet::isAttacker);
    }

    public boolean hasLost() {
        return getFleets().stream().allMatch(BattleFleet::isAttacker);
    }

    public boolean hasDrawn() {
        return getFleets().stream().anyMatch(BattleFleet::isAttacker) && getFleets().stream().anyMatch(fleet -> !fleet.isAttacker());
    }

    @Override
    public void updateFleet(BattleFleet battleFleet) {

    }

    @Override
    public void removeFleet(BattleFleet battleFleet) {

        getFleets().remove(battleFleet);
        removedFleets.add(battleFleet);

    }

    @Override
    public void removeFort(BattleFort battleFort) {

        // ? this never happens

    }

    @Override
    public void returnAllFleets() {
        for (BattleFleet battleFleet : getAllFleets()) {
            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(battleFleet.getShipTeamId());

            if (fleet == null) {
                continue;
            }

            Commander commander = fleet.getCommander();
            if (commander != null) {
                commander.save();
            }

            fleet.setMatch(false);
            fleet.setFleetMatch(null);
            fleet.setGalaxyId(fleet.getUser().getGalaxyId());
            fleet.save();
        }

    }

    @Override
    public AttackSideType fortressAttackType() {

        return AttackSideType.NONE;
    }

    @Override
    public boolean hasUser(int guid) {
        return sourceIds.contains(guid) || targetIds.contains(guid);
    }

    public boolean addPlayer(User user, List<Integer> fleetIds, boolean isTarget) {
        List<Fleet> fleets = BattleService.getInstance().getRealFleets(user, fleetIds);

        if (fleets == null) {
            return false;
        }

        for (Fleet fleet : fleets) {
            if (fleet.getGuid() != user.getGuid()) {
                return false;
            }
            if (fleet.isMatch()) {
                return false;
            }
        }

        if (isTarget) {
            getTargetIds().add(user.getGuid());
            getFleets().addAll(BattleService.getInstance().createBattleFleets(fleets, true, this));
        } else {
            getSourceIds().add(user.getGuid());
            getFleets().addAll(BattleService.getInstance().createBattleFleets(fleets, false, this));
        }
        return true;
    }

    public boolean removePlayer(int guid) {
        if (!getPause().get() || !hasUser(guid)) {
            return false;
        }

        List<BattleFleet> fleets = getAllFleets(guid);
        User targetUser = UserService.getInstance().getUserCache().findByGuid(guid);
        for (BattleFleet battleFleet : fleets) {
            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(battleFleet.getShipTeamId());
            if (fleet == null) {
                continue;
            }
            Commander commander = fleet.getCommander();
            if (commander != null) {
                commander.save();
            }
            fleet.setMatch(false);
            fleet.setFleetMatch(null);
            fleet.setGalaxyId(targetUser.getGalaxyId());
            fleet.save();
        }

        getFleets().removeAll(fleets);
        getSourceIds().removeIf(id -> id == guid);
        getTargetIds().removeIf(id -> id == guid);

        Optional<LoggedGameUser> target = LoginService.getInstance().getGame(guid);
        if (target.isPresent()) {
            LoggedGameUser t = target.get();
            ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
            response.setEctypeId((short) 0);
            response.setGateId(UnsignedChar.of(0));
            response.setState((byte) 0);
            t.getSmartServer().send(response);
        }

        return true;
    }

    public List<BattleFleet> getAllFleets(int guid) {

        return getAllFleets().stream().filter(fleet -> fleet.getGuid() == guid).collect(Collectors.toList());
    }

    public List<BattleFleet> getAllFleets() {

        return Stream.concat(getFleets().stream(), getRemovedFleets().stream()).collect(Collectors.toList());
    }

    public boolean start() {
        GameBattle gameBattle = BattleService.getInstance().getBattle(this);
        if (gameBattle == null) {
            BotLogger.error("ChampMatch: Failed to start champ match");
            return false;
        }
        this.getPause().set(false);
        gameBattle.getThread().start();
        return true;
    }

    private int getRewardsByPlace(int place) {
        if (MathUtil.isInRange(place, 1, 3)) {
            return 900;
        }
        if (MathUtil.isInRange(place, 4, 10)) {
            return 800;
        }
        if (MathUtil.isInRange(place, 11, 20)) {
            return 600;
        }
        if (MathUtil.isInRange(place, 21, 30)) {
            return 300;
        }
        return 150;
    }

}

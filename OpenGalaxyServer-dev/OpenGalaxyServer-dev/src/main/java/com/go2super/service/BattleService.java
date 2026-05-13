package com.go2super.service;

import com.go2super.database.entity.*;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.MatchType;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.database.entity.type.SpaceFortType;
import com.go2super.obj.game.*;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.InstanceType;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.GameInstance;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import com.go2super.packet.construction.ResponseBuildInfoPacket;
import com.go2super.packet.fight.*;
import com.go2super.packet.instance.ResponseEctypeStatePacket;
import com.go2super.packet.ship.ResponseDeleteShipTeamBroadcastPacket;
import com.go2super.packet.ship.ResponseGalaxyShipPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.ResearchData;
import com.go2super.resources.data.meta.PlayerFleetMeta;
import com.go2super.service.battle.*;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.calculator.ShipTechs;
import com.go2super.service.battle.match.*;
import com.go2super.service.battle.pathfinder.GO2Node;
import com.go2super.service.battle.type.BattleResultType;
import com.go2super.service.battle.type.StopCause;
import com.go2super.service.champ.ChampPhase;
import com.go2super.service.raids.Raid;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class BattleService {

    @Getter
    private final CopyOnWriteArrayList<GameBattle> battles = new CopyOnWriteArrayList<>();

    private static BattleService instance;

    public BattleService() {

        instance = this;

    }

    public void setup() {

        for (Fleet active : PacketService.getInstance().getFleetCache().findAll()) {

            if (active.getGuid() == -1) {

                PacketService.getInstance().getFleetCache().delete(active);
                continue;

            }

            User user = UserService.getInstance().getUserCache().findByGuid(active.getGuid());

            if (user == null) {

                PacketService.getInstance().getFleetCache().delete(active);
                continue;

            }

            active.setGalaxyId(user.getPlanet().getPosition().galaxyId());

            active.setMatch(false);
            active.setFleetMatch(null);
            active.setFleetInitiator(null);
            active.setFleetTransmission(null);

            active.save();

        }

        List<Fleet> warFleets = PacketService.getInstance().getFleetCache().getInWarFleets();

        for (Fleet active : warFleets) {

            User user = UserService.getInstance().getUserCache().findByGuid(active.getGuid());
            active.setGalaxyId(user.getPlanet().getPosition().galaxyId());

            active.setMatch(false);
            active.setFleetMatch(null);
            active.setFleetInitiator(null);
            active.setFleetTransmission(null);

            active.save();

        }

        for (Commander commander : CommanderService.getInstance().getCommanderCache().findAll()) {
            if (commander.getUserId() == -1) {
                commander.delete();
            }
        }

    }


    private MatchRunnable run(Match match) {

        if (match == null) {
            return null;
        }
        switch(match.getClass().getName().replaceAll("com.go2super.service.battle.match.", "")){
            case "ArenaMatch":
            case "ChampMatch":
                return run(new MatchRunnable(match), false);
            default:{
                return run(new MatchRunnable(match), true);
            }
        }
    }
/*
    private MatchRunnable run(LeagueMatch match) {

        if (match == null) {
            return null;
        }

        return run(new MatchRunnable(match), true);

    }


    private MatchRunnable run(IglMatch match) {

        if (match == null) {
            return null;
        }

        return run(new MatchRunnable(match), true);

    }

    private MatchRunnable run(ChampMatch match) {

        if (match == null) {
            return null;
        }

        return run(new MatchRunnable(match), false);

    }


    private MatchRunnable run(InstanceMatch match) {

        if (match == null) {
            return null;
        }

        return run(new MatchRunnable(match), true);

    }*/

    private MatchRunnable run(MatchRunnable matchRunnable, boolean start) {

        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().equals(matchRunnable)).findAny();

        if (current.isPresent()) {
            System.out.println("The same battle had existed");
            return null;
        }

        Thread battleThread = new Thread(matchRunnable);
        battleThread.setName("game-battle-" + matchRunnable.getMatch().getMatchType().name().toLowerCase());
        if (start) {
            battleThread.start();
        }

        battles.add(GameBattle.builder()
                .runnable(matchRunnable)
                .thread(battleThread)
                .build());

        return matchRunnable;

    }

    public boolean isRunning(Match match) {

        return getRunnable(match) != null;
    }
    public List<GameBattle> getBattles(int guid){
        return battles.stream().filter(x -> x.getMatch().getFleets().stream().anyMatch(y -> y.getGuid() == guid)).toList();
    }
    public GameBattle getBattle(String matchId) {

        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(matchId)).findAny();

        return current.orElse(null);

    }

    public GameBattle getBattle(Match match) {

        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(match.getId())).findAny();

        return current.orElse(null);

    }

    public MatchRunnable getRunnable(Match match) {

        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(match.getId())).findAny();

        return current.map(GameBattle::getRunnable).orElse(null);

    }

    public MatchRunnable getRunnable(String matchId) {

        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(matchId)).findAny();

        return current.map(GameBattle::getRunnable).orElse(null);

    }

    public Match getCurrent(Commander commander) {

        Fleet fleet = PacketService.getInstance().getFleetCache().findByCommanderId(commander.getCommanderId());
        if (fleet == null) {
            return null;
        }

        if (fleet.isInMatch() || fleet.isInTransmission() || fleet.isMatch()) {
            return fleet.getCurrentMatch();
        }
        return null;

    }

    public Match getCurrent(Fleet fleet) {

        FleetMatch fleetMatch = fleet.getFleetMatch();

        if (fleetMatch == null) {
            return null;
        }

        for (GameBattle gameBattle : battles) {
            if (gameBattle.getRunnable().getMatch().getId().equals(fleetMatch.getMatch())) {
                return gameBattle.getRunnable().getMatch();
            }
        }

        return null;

    }

    public List<GameBattle> getBattles(MatchType matchType) {

        List<GameBattle> result = new ArrayList<>();

        for (GameBattle gameBattle : battles) {

            Match match = gameBattle.getRunnable().getMatch();

            if (match.getMatchType() != matchType) {
                continue;
            }

            result.add(gameBattle);

        }

        return result;

    }

    public List<GameBattle> findBattlesMatchType(MatchType matchType) {
        List<GameBattle> result = new ArrayList<>();
        for (GameBattle gameBattle : battles) {
            Match match = gameBattle.getRunnable().getMatch();
            if (match.getMatchType() != matchType) {
                continue;
            }
            result.add(gameBattle);
        }
        return result;
    }

    public Optional<ArenaMatch> getArenaByRoomId(int roomId) {

        for (GameBattle gameBattle : battles) {
            if (gameBattle.getMatch().getMatchType() == MatchType.ARENA_MATCH) {
                ArenaMatch arenaMatch = (ArenaMatch) gameBattle.getMatch();
                if (arenaMatch.getSourceGuid() == roomId) {
                    return Optional.of(arenaMatch);
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Match> getVirtual(int guid) {

        for (GameBattle gameBattle : battles) {
            if (gameBattle.getMatch().getMatchType().isVirtual()) {
                if (gameBattle.getMatch().hasUser(guid)) {
                    return Optional.of(gameBattle.getMatch());
                }
            }
        }
        return Optional.empty();
    }

    public Optional<Match> getVirtualViewing(LoggedGameUser loggedGameUser) {

        if (loggedGameUser.getMatchViewing() == null) {
            return Optional.empty();
        }
        var match = battles.stream().filter(x -> x.getMatch().getMatchType().isVirtual() && x.getMatch().getId().equals(loggedGameUser.getMatchViewing())).toList();
        if (match.size() > 0) {
            return Optional.of(match.get(0).getMatch());
        }
        return Optional.empty();
    }

    public List<Match> getCurrent(int guid, MatchType matchType) {

        List<Match> matches = new ArrayList<>();

        for (GameBattle gameBattle : battles) {

            Match match = gameBattle.getRunnable().getMatch();

            if (match.getMatchType() != matchType) {
                continue;
            }

            switch (matchType) {

                case INSTANCE_MATCH:

                    InstanceMatch instanceMatch = (InstanceMatch) match;

                    if (instanceMatch.getGuid() == guid) {
                        matches.add(match);
                    }

                    break;

                case ARENA_MATCH:

                    ArenaMatch arenaMatch = (ArenaMatch) match;

                    if (arenaMatch.getSourceGuid() == guid ||
                            arenaMatch.getTargetGuid() == guid) {
                        matches.add(arenaMatch);
                    }

                    break;

                case LEAGUE_MATCH:
                    LeagueMatch leagueMatch = (LeagueMatch) match;
                    if (leagueMatch.getSourceGuid() == guid || leagueMatch.getTargetGuid() == guid) {
                        matches.add(leagueMatch);
                    }

                default:
                    break;

            }

        }

        return matches;

    }

    public void sendMatchRoundToViewers(Match match) {

        if (match == null) {
            return;
        }

        ResponseFightBoutBegPacket fightBoutBegPacket = new ResponseFightBoutBegPacket();

        fightBoutBegPacket.setGalaxyId(match.getGalaxyId());
        fightBoutBegPacket.setGalaxyMapId(0);

        fightBoutBegPacket.setBoutId((short) (Math.max(match.getRound(), 0)));

        if (match.isWatchMode()) {
            fightBoutBegPacket.setMillis(50L);
            match.getPackets().add(fightBoutBegPacket);
        } else {
            for (LoggedGameUser loggedGameUser : match.getViewers()) {
                loggedGameUser.getSmartServer().send(fightBoutBegPacket);
            }
        }

    }

    public synchronized MatchRunnable makeLeagueMatch(User user1, List<Integer> fleetIds1) {
        List<Fleet> fleets1 = getRealFleets(user1, fleetIds1);
        if (fleets1 == null) {
            return null;
        }

        for (Fleet fleet : fleets1) {
            if (fleet.isMatch()) {
                return null;
            }
        }

        LeagueMatch match = makeLeague(user1, fleets1);

        MatchRunnable runnable = run(match);
        return runnable;

    }

    public synchronized MatchRunnable makeChampMatch(int id, ChampPhase.Phase phase) {
        ChampMatch match = makeChamps(id, phase);
        return run(match);
    }

    public synchronized  MatchRunnable makeRaidMatch(Raid raid, int attacker, List<Integer> fleets){
        RaidMatch match = makeRaid(raid, attacker, getRealFleets(UserService.getInstance().getUserCache().findByGuid(attacker), fleets));
        return run(match);
    }


    public synchronized MatchRunnable makeWarMatch(Planet planet) {

        if (planet.isInWar()) {
            return null;
        }

        if (planet.getType() == PlanetType.HUMAROID_PLANET) {

            HumaroidPlanet humaroidPlanet = (HumaroidPlanet) planet;
            List<Fleet> fleets = humaroidPlanet.getFleets();

            if (fleets.stream().noneMatch(fleet -> fleet.getGuid() == -1)) {

                InstanceData instanceData = ResourceManager.getHumaroids().getHumaroid(humaroidPlanet.getCurrentLevel());
                if (instanceData != null) {
                    instanceData.generatePirateEnemies(planet.getPosition().galaxyId(), false, true);
                }

            }

        } else if (planet.getType() == PlanetType.RESOURCES_PLANET) {

            ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
            List<Fleet> fleets = resourcePlanet.getFleets();

            if (resourcePlanet.getCorp().isEmpty() && fleets.stream().noneMatch(fleet -> fleet.getGuid() == -1)) {

                InstanceData instanceData = ResourceManager.getRBP().getInstance();
                if (instanceData != null) {
                    instanceData.generatePirateEnemies(planet.getPosition().galaxyId(), false, true);
                }

            }

        }

        List<Fleet> fleets = PacketService.getInstance().getFleetCache().findAllByGalaxyId(planet.getPosition().galaxyId());

        WarMatch match = makeWar(planet, fleets);

        return run(match);

    }

    public synchronized MatchRunnable makeArenaMatch(User user, List<Integer> fleetIds, int passKey) {

        if (fleetIds.size() > 60 || fleetIds.size() < 1) {
            return null;
        }

        List<Fleet> fleets = getRealFleets(user, fleetIds);

        if (fleets == null) {
            return null;
        }

        for (Fleet fleet : fleets) {

            if (fleet.isMatch()) {
                return null;
            }

        }

        ArenaMatch match = makeArena(user, fleets, passKey);

        return run(match);

    }

    public synchronized MatchRunnable makeInstanceMatch(User user, List<Integer> fleets, GameInstance instance) {

        InstanceMatch match = filterInstanceMatch(user, fleets, instance);

        if (match == null) {
            return null;
        }

        return run(match);

    }

    public synchronized MatchRunnable makeIglMatch(User user1, User user2, List<Integer> fleetIds1, List<Integer> fleetIds2) {
        if (fleetIds1.size() > 12 || fleetIds2.size() > 12) {
            return null;
        }

        List<Fleet> fleets1 = getCopyFleets(user1, fleetIds1);
        List<Fleet> fleets2 = getCopyFleets(user2, fleetIds2);
        IglMatch match = makeIgl(user1, user2, fleets1, fleets2);
        MatchRunnable runnable = run(match);
        return runnable;
    }

    private synchronized InstanceMatch filterInstanceMatch(User user, List<Integer> fleets, GameInstance instance) {

        return startInstanceMatch(user, fleets, instance);
    }

    public void stopMatch(Match match, StopCause stopCause) {

        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(match.getId())).findAny();
        match.stop(stopCause);

        if (current.isEmpty()) {
            return;
        }

        GameBattle gameBattle = current.get();
        MatchRunnable matchRunnable = getRunnable(match);

        if (matchRunnable != null) {

            matchRunnable.getInterrupted().set(true);
            battles.remove(gameBattle);

        }

    }

    public boolean stopMatch(String matchId) {
        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(matchId)).findAny();
        if (current.isEmpty()) {
            return false;
        }
        stopMatch(current.get().getMatch(), StopCause.AUTOMATIC);
        return true;
    }

    public GameBattle findById(String matchId) {
        Optional<GameBattle> current = battles.stream().filter(battle -> battle.getRunnable().getMatch().getId().equals(matchId)).findAny();
        return current.orElse(null);
    }

    public void stopAllMatches() {
        List<GameBattle> matches = battles.stream().toList();
        for (GameBattle gameBattle : matches) {
            stopMatch(gameBattle.getRunnable().getMatch(), StopCause.AUTOMATIC);
        }
    }

    public void stopMatches(MatchType matchType) {
        List<GameBattle> matches = getBattles(matchType);
        for (GameBattle gameBattle : matches) {
            stopMatch(gameBattle.getRunnable().getMatch(), StopCause.AUTOMATIC);
        }
    }

    public void stopCoordinate(GalaxyTile coordinate) {
        List<GameBattle> matches = getBattles(MatchType.PVP_MATCH);
        for (GameBattle gameBattle : matches) {
            Match match = gameBattle.getRunnable().getMatch();
            if (match.getGalaxyId() == coordinate.galaxyId()) {
                stopMatch(match, StopCause.AUTOMATIC);
            }
        }
    }

    private synchronized InstanceMatch startInstanceMatch(User user, List<Integer> fleetIds, GameInstance gameInstance) {

        int maxPlayerSize = 4;

        switch (gameInstance.getType()) {

            case INSTANCE:
                boolean legalInstance = gameInstance.getData().getId() <= user.getStats().getInstance();
                if (!legalInstance) {
                    return null;
                }
                maxPlayerSize = gameInstance.getData().getPlayerFleets().size();
                break;

            case RESTRICTED:
                if (user.getRestrictedUsedEntries() > 3) {
                    return null;
                }
                if (user.getRestrictedUsedEntries() == 3) {

                    Prop passport = user.getInventory().getProp(932);
                    if (passport == null) {
                        return null;
                    }

                    Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(passport, 1);
                    if (!removedProp.getKey()) {
                        return null;
                    }

                }

                user.getStats().setRestrictedUsedEntries(user.getRestrictedUsedEntries() + 1);

                user.update();
                user.save();

                maxPlayerSize = gameInstance.getData().getPlayerFleets().size();
                break;

            case TRIALS:
                int displayInstance = ((gameInstance.getData().getId() - 62) + 10);
                if (user.getTrial() != displayInstance) {
                    return null;
                }
                break;

            case CONSTELLATION:

                Prop constellationPass = user.getInventory().getProp(953);
                if (constellationPass == null) {
                    return null;
                }

                Pair<Boolean, Boolean> removedProp = user.getInventory().removeProp(constellationPass, 1);
                if (!removedProp.getKey()) {
                    return null;
                }

                maxPlayerSize = gameInstance.getData().getPlayerFleets().size();
                break;

        }

        List<Fleet> fleets = getRealFleets(user, fleetIds);
        if (fleets == null) {
            return null;
        }
        if (fleets.size() > maxPlayerSize) {
            return null;
        }

        InstanceMatch match = makeInstance(user, gameInstance.getData(), gameInstance.getType());

        match.setStartDate(new Date().getTime());
        match.getFleets().addAll(createBattleFleets(fleets, gameInstance.getData(), match));

        return match;

    }

    public synchronized ChampMatch makeChampsWaitingRoom() {
        ChampMatch match = new ChampMatch();
        match.getPause().set(true);
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.CHAMPION_MATCH);
        match.setEctype(0);
        match.setGalaxyId(-1);
        match.getTags().add(BattleTag.of("champ", 0));
        match.setRound(-1);
        match.setCurrentRound(BattleRound.builder().build());
        return match;
    }

    private synchronized ChampMatch makeChamps(int id, ChampPhase.Phase phase) {
        ChampMatch match = new ChampMatch();
        match.getPause().set(true);
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.CHAMPION_MATCH);
        match.setEctype(1002);
        match.setGalaxyId(-1);
        match.getTags().add(BattleTag.of("champ", 0));
        match.setRound(-1);
        match.setCurrentRound(BattleRound.builder().build());
        match.setMaxRound(100);
        match.setRoomId(id);
        match.setPhase(phase);
        return match;
    }

    private synchronized RaidMatch makeRaid(Raid raid, int attacker, List<Fleet> interceptFleets){
        RaidMatch match = new RaidMatch();
        match.getPause().set(false);
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.RAIDS_MATCH);
        match.setEctype(0);
        match.setGalaxyId(-1);
        match.setAttacker(attacker);
        match.setDefender1(raid.getFirstGuid());
        match.setDefender2(raid.getSecondGuid());
        match.getFleets().addAll(createBattleFleets(getRealFleets(UserService.getInstance().getUserCache().findByGuid(raid.getFirstGuid()), raid.getFirstDefenceFleets()), true, match));
        match.getFleets().addAll(createBattleFleets(getRealFleets(UserService.getInstance().getUserCache().findByGuid(raid.getSecondGuid()), raid.getSecondDefenceFleets()), true, match));
        match.getFleets().addAll(createBattleFleets(interceptFleets, false, match));
        match.setRound(-1);
        match.setCurrentRound(BattleRound.builder().build());
        match.getTags().add(BattleTag.of("instance", 0));
        return match;
    }
    private synchronized LeagueMatch makeLeague(User user1, List<Fleet> fleet1) {
        LeagueMatch match = new LeagueMatch();
        match.getPause().set(true);
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.LEAGUE_MATCH);
        match.setSourceGuid(user1.getGuid());
        match.setSourceUserId(user1.getUserId());
        match.setEctype(1000);
        match.setGalaxyId(-1);
        match.getFleets().addAll(createBattleFleets(fleet1, false, match));
        match.getTags().add(BattleTag.of("instance", 1000));
        match.setLeagueId(user1.getCurrentLeague());
        match.setRound(-1);
        match.setCurrentRound(BattleRound.builder().build());
        match.setLeagueId(user1.getCurrentLeague());
        return match;
    }

    private synchronized WarMatch makeWar(Planet target, List<Fleet> start) {

        WarMatch match = new WarMatch(target);

        match.getPause().set(false);
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.PVP_MATCH);
        match.setGalaxyId(target.getPosition().galaxyId());
        match.setMetadata(new BattleMetadata());

        if (target.getType() == PlanetType.USER_PLANET) {

            UserPlanet userPlanet = (UserPlanet) target;
            Optional<User> optionalUser = userPlanet.getUser();

            if (optionalUser.isPresent()) {

                User targetUser = optionalUser.get();

                targetUser.update();
                targetUser.save();

            }

        }

        List<Fleet> enemies = start.stream().filter(fleet -> fleet.getFleetInitiator() != null && fleet.getFleetInitiator().getJumpType() == JumpType.ATTACK).collect(Collectors.toList());
        List<Fleet> allies = start.stream().filter(fleet -> !enemies.contains(fleet)).collect(Collectors.toList());

        match.getForts().addAll(createBattleForts(target));
        match.getFleets().addAll(createBattleFleets(enemies, false, match));
        match.getFleets().addAll(createBattleFleets(allies, true, match));

        match.getTags().add(BattleTag.of("initiator", -1));
        return match;

    }

    private synchronized ArenaMatch makeArena(User user, List<Fleet> fleets, int password) {

        ArenaMatch match = new ArenaMatch(password);

        match.getPause().set(true);
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.ARENA_MATCH);

        match.setGalaxyId(-1);
        match.setEctype(1001);

        match.setSourceGuid(user.getGuid());
        match.setSourceUserId(user.getUserId());
        match.setSourceUsername(user.getUsername());
        match.setSourceSend(countShips(fleets));

        match.getFleets().addAll(createBattleFleets(fleets, false, match));

        match.getTags().add(BattleTag.of("instance", 1001));
        return match;

    }

    private synchronized InstanceMatch makeInstance(User user, InstanceData data, InstanceType type) {

        InstanceMatch match = new InstanceMatch(data);

        match.getPause().set(false);
        match.setId(UUID.randomUUID().toString());
        match.setType(type);
        match.setMatchType(MatchType.INSTANCE_MATCH);
        match.setGuid(user.getGuid());

        match.setGalaxyId(-1);
        match.setEctype(data.getId());

        match.getFleets().addAll(data.getEnemyFleets(match));

       /*
        match.getForts().addAll(data.getEnemyForts());
        for (BattleFort fort : match.getForts()) {
            fort.setDefender(true);
        }
        */

        match.getTags().add(BattleTag.of("instance", data.getId()));

        int maxRound = 20 + (match.getFleets().size() + match.getForts().size());
        maxRound = Math.min(maxRound, 100);

        match.setMaxRound(maxRound);
        return match;

    }


    private synchronized IglMatch makeIgl(User user, User user2, List<Fleet> fleets1, List<Fleet> fleets2) {
        IglMatch match = new IglMatch();
        match.setId(UUID.randomUUID().toString());
        match.setMatchType(MatchType.IGL_MATCH);
        match.setGalaxyId(-1);
        match.setEctype(1002);
        match.setSourceGuid(user.getGuid());
        match.setSourceUserId(user.getUserId());
        match.setTargetGuid(user2.getGuid());
        match.setTargetUserId(user2.getUserId());
        match.getFleets().addAll(createBattleFleetsIgl(fleets1, false, match));
        match.getFleets().addAll(createBattleFleetsIgl(fleets2, true, match));
        match.getTags().add(BattleTag.of("igl", 1002));
        match.setWatchMode(true);
        match.setMaxRound(30);
        match.getPause().set(false);
        return match;

    }

    public List<List<ArenaMatch>> getArenasPages(boolean waiting) {

        return Lists.partition(getArenas(waiting), 5);
    }

    public Optional<LeagueMatch> findEmptyLeagueRoom(int rank) {
        List<GameBattle> battles = getBattles(MatchType.LEAGUE_MATCH);
        return battles.stream().filter(battle -> battle.getMatch().getPause().get()).map(battle -> (LeagueMatch) battle.getMatch())
                .filter(league -> league.getLeagueId() == rank)
                .filter(league -> league.getSourceGuid() == -1 || league.getTargetGuid() == -1)
                .findAny();
    }

    public void endActiveChampMatches() {
        getBattles(MatchType.CHAMPION_MATCH)
                .stream().filter(x -> !x.getMatch().getPause().get() && !x.getMatch().isEnded())
                .forEach(battle -> stopMatch(battle.getMatch(), StopCause.AUTOMATIC));
    }

    public List<Integer> findLeagueUsersByRank(int rank) {
        List<Integer> uids = new ArrayList<>();
        List<GameBattle> battles = getBattles(MatchType.LEAGUE_MATCH);
        for (GameBattle battle : battles) {
            if (battle.getMatch().getPause().get()) {
                LeagueMatch league = (LeagueMatch) battle.getMatch();
                if (league.getLeagueId() == rank) {
                    if (league.getSourceGuid() != -1 || league.getTargetGuid() != -1) {
                        if (league.getSourceUserId() != -1) {
                            uids.add(league.getSourceGuid());
                        }
                        if (league.getTargetUserId() != -1) {
                            uids.add(league.getTargetGuid());
                        }
                    }
                }
            }
        }
        return uids;
    }

    public Match findBy(GalaxyTile galaxyTile) {
        for (MatchType matchType : MatchType.getAllNonVirtual()) {
            List<GameBattle> battles = getBattles(matchType);
            for (GameBattle battle : battles) {
                Match match = battle.getMatch();
                if (match.getGalaxyId() == galaxyTile.galaxyId()) {
                    return match;
                }
            }
        }

        return null;
    }

    public List<GameBattle> findByGuid(int guid) {
        List<GameBattle> result = new ArrayList<>();
        for (MatchType matchType : MatchType.values()) {
            List<GameBattle> battles = getBattles(matchType);
            for (GameBattle battle : battles) {
                Match match = battle.getMatch();
                if (match.hasUser(guid)) {
                    result.add(battle);
                }
            }
        }
        return result;
    }

    public Map<Integer, List<LeagueMatch>> getLeaguesWaitingByRank() {
        List<GameBattle> battles = getBattles(MatchType.LEAGUE_MATCH);
        return battles.stream().filter(battle -> battle.getMatch().getPause().get()).map(battle -> (LeagueMatch) battle.getMatch())
                .collect(Collectors.groupingBy(LeagueMatch::getLeagueId));
    }

    public Optional<LeagueMatch> findLeagueByGuid(int guid) {
        List<GameBattle> battles = getBattles(MatchType.LEAGUE_MATCH);
        return battles.stream().map(battle -> (LeagueMatch) battle.getMatch()).filter(league -> league.getSourceGuid() == guid || league.getTargetGuid() == guid).findAny();
    }

    public Optional<ArenaMatch> getArenaByOwner(int guid, boolean waiting) {

        List<GameBattle> battles = getBattles(MatchType.ARENA_MATCH);
        List<ArenaMatch> arenas = battles.stream().filter(battle -> waiting == battle.getMatch().getPause().get()).map(battle -> (ArenaMatch) battle.getMatch()).toList();

        return arenas.stream().filter(arena -> arena.getSourceGuid() == guid).findAny();

    }

    public Optional<ChampMatch> findChampMatchByGuid(int guid) {
        List<GameBattle> battles = getBattles(MatchType.CHAMPION_MATCH);

        List<ChampMatch> champs = battles.stream().map(battle -> (ChampMatch) battle.getMatch()).toList();
        return champs.stream().filter(x -> x.getTargetIds().contains(guid) || x.getSourceIds().contains(guid)).findAny();

    }

    public Optional<ChampMatch> findChampMatchByRoomId(int roomId) {
        List<GameBattle> battles = getBattles(MatchType.CHAMPION_MATCH);
        List<ChampMatch> champs = battles.stream().map(battle -> (ChampMatch) battle.getMatch()).toList();
        return champs.stream().filter(x -> x.getRoomId() == roomId).findAny();
    }

    public List<ArenaMatch> getArenas(boolean waiting) {

        List<GameBattle> battles = getBattles(MatchType.ARENA_MATCH);

        return battles.stream().filter(battle -> waiting == battle.getMatch().getPause().get()).map(battle -> (ArenaMatch) battle.getMatch()).collect(Collectors.toList());

    }

    public List<WarMatch> getWars() {

        List<GameBattle> battles = getBattles(MatchType.PVP_MATCH);

        return battles.stream().map(battle -> (WarMatch) battle.getMatch()).collect(Collectors.toList());

    }

    public List<BattleFort> createBattleForts(Planet planet) {

        List<BattleFort> result = new ArrayList<>();
        List<UserBuilding> buildings = new ArrayList<>();

        int guid = -1;
        long userId = -1;

        switch (planet.getType()) {

            case USER_PLANET -> {

                UserPlanet userPlanet = (UserPlanet) planet;
                User user = userPlanet.getUser().orElse(null);

                assert user != null;
                LinkedList<UserBuilding> userBuildings = user.getBuildings().getBuildings();
                userBuildings = userBuildings.stream().filter(building -> building.getData().getType().equals("space")).collect(Collectors.toCollection(LinkedList::new));

                buildings.addAll(userBuildings);

                guid = user.getGuid();
                userId = user.getUserId();

                for (UserBuilding building : buildings) {

                    SpaceFortType type = SpaceFortType.getFortType(true, building.getData().getId());
                    BattleFort fort = new BattleFort(type, building.getLevelId() + 1);

                    fort.setDefender(true);
                    fort.setFortId(building.getIndex());
                    fort.setPosX(building.getX());
                    fort.setPosY(building.getY());

                    fort.setGuid(guid);
                    fort.setUserId(userId);

                    result.add(fort);

                }

                break;

            }

            case HUMAROID_PLANET -> {
                break;
            }

            case RESOURCES_PLANET -> {

                ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
                RBPBuildings rbpBuildings = resourcePlanet.getBuildings();

                for (RBPBuilding building : rbpBuildings.getBuildings()) {

                    SpaceFortType type = SpaceFortType.getFortType(false, building.getData().getId());
                    BattleFort fort = new BattleFort(type, building.getLevelId());

                    fort.setDefender(true);
                    fort.setFortId(building.getIndex());
                    fort.setPosX(building.getX());
                    fort.setPosY(building.getY());

                    fort.setGuid(guid);
                    fort.setUserId(userId);

                    result.add(fort);

                }

                PacketService.getInstance().getPlanetCache().save(resourcePlanet);
                break;

            }

        }

        return result;

    }

    public List<BattleFleet> createBattleFleets(List<Fleet> realFleets, boolean defender, Match match) {
        List<BattleFleet> result = new ArrayList<>();
        Map<Integer, ShipTechs> techs = new HashMap<>();

        List<ResearchData> allResearchData = new ArrayList<>();
        allResearchData.addAll(ResourceManager.getScience().getWeapons());
        allResearchData.addAll(ResourceManager.getScience().getDefense());

        ShipTechs allShipTechs = new ShipTechs();
        allShipTechs.passData(allResearchData);

        for (Fleet fleet : realFleets) {
            if (fleet.isInMatch() || fleet.isInTransmission()) {
                continue;
            }

            Commander commander = fleet.getCommander();
            if (commander == null) {
                continue;//throw new NullPointerException("Commander is null (commanderId: " + fleet.getCommanderId() + ", fleet: " + fleet + ")");
            }

            // * BattleFleet creation
            BattleFleet battleFleet = new BattleFleet();

            battleFleet.setName(fleet.getName());
            battleFleet.setDefender(defender);
            battleFleet.setBodyId(fleet.getBodyId());
            battleFleet.setPosX(fleet.getPosX());
            battleFleet.setPosY(fleet.getPosY());
            battleFleet.setDirection(fleet.getDirection());

            battleFleet.setHe3(fleet.getHe3());
            battleFleet.setMaxHe3(fleet.getMaxHe3());
            battleFleet.setMaxRounds(10);
            battleFleet.setJoinRound(0);
            battleFleet.setGuid(fleet.getGuid());
            battleFleet.setShipTeamId(fleet.getShipTeamId());
            battleFleet.setTarget(fleet.getRangeType());
            battleFleet.setTargetInterval(fleet.getPreferenceType());
            battleFleet.setBattleCommander(commander.createBattleCommander(fleet.getAdditionalGrowth(), null));

            if (fleet.isForceTechs()) {
                battleFleet.setTechs(allShipTechs);
            } else {
                if (techs.containsKey(fleet.getGuid())) {
                    battleFleet.setTechs(techs.get(fleet.getGuid()));
                } else {
                    User user = fleet.getUser();
                    if (user != null) {
                        techs.put(fleet.getGuid(), new ShipTechs(user.getTechs().getTechs()));
                    } else {
                        techs.put(fleet.getGuid(), new ShipTechs(new ArrayList<>()));
                    }
                    battleFleet.setTechs(techs.get(fleet.getGuid()));
                }
            }

            battleFleet.setTeam(BattleFleetTeam.fromShipTeamBody(fleet.getGuid(), fleet.getFleetBody(), battleFleet.getBattleCommander(), battleFleet.getTechs()));

            result.add(battleFleet);

            // * Fleet changes
            fleet.setMatch(true);
            fleet.setFleetMatch(FleetMatch.builder()
                    .galaxyId(-1)
                    .matchType(match.getMatchType())
                    .match(match.getId())
                    .build());

            if (match.getMatchType().isVirtual()) {

                ResponseDeleteShipTeamBroadcastPacket response = new ResponseDeleteShipTeamBroadcastPacket();

                response.setGalaxyMapId(0);
                response.setGalaxyId(fleet.getGalaxyId());
                response.setShipTeamId(fleet.getShipTeamId());

                for (LoggedGameUser loggedGameUser : LoginService.getInstance().getPlanetViewers(fleet.getGalaxyId())) {
                    loggedGameUser.getSmartServer().send(response);
                }

            }

            if (match.getMatchType().isVirtual()) {
                fleet.setGalaxyId(-1);
            }

            //fleet.save();

            // * Commander update
            //commander.save();

        }

        return result;
    }


    public List<BattleFleet> createBattleFleetsIgl(List<Fleet> realFleets, boolean defender, Match match) {
        List<BattleFleet> result = new ArrayList<>();
        Map<Integer, ShipTechs> techs = new HashMap<>();

        List<ResearchData> allResearchData = new ArrayList<>();
        allResearchData.addAll(ResourceManager.getScience().getWeapons());
        allResearchData.addAll(ResourceManager.getScience().getDefense());

        ShipTechs allShipTechs = new ShipTechs();
        allShipTechs.passData(allResearchData);

        for (Fleet fleet : realFleets) {
            Commander commander = fleet.getCommander();
            if (commander == null) {
                continue;
            }

            // * BattleFleet creation
            BattleFleet battleFleet = new BattleFleet();

            battleFleet.setName(fleet.getName());
            battleFleet.setDefender(defender);
            battleFleet.setBodyId(fleet.getBodyId());
            battleFleet.setPosX(fleet.getPosX());
            battleFleet.setPosY(fleet.getPosY());
            battleFleet.setDirection(fleet.getDirection());

            battleFleet.setHe3(fleet.getHe3());
            battleFleet.setMaxHe3(fleet.getMaxHe3());
            battleFleet.setMaxRounds(10);
            battleFleet.setJoinRound(0);
            battleFleet.setGuid(fleet.getGuid());
            battleFleet.setShipTeamId(fleet.getShipTeamId());
            battleFleet.setTarget(fleet.getRangeType());
            battleFleet.setTargetInterval(fleet.getPreferenceType());
            battleFleet.setBattleCommander(commander.createBattleCommander(fleet.getAdditionalGrowth(), null));

            if (fleet.isForceTechs()) {
                battleFleet.setTechs(allShipTechs);
            } else {
                if (techs.containsKey(fleet.getGuid())) {
                    battleFleet.setTechs(techs.get(fleet.getGuid()));
                } else {
                    User user = fleet.getUser();
                    if (user != null) {
                        techs.put(fleet.getGuid(), new ShipTechs(user.getTechs().getTechs()));
                    } else {
                        techs.put(fleet.getGuid(), new ShipTechs(new ArrayList<>()));
                    }
                    battleFleet.setTechs(techs.get(fleet.getGuid()));
                }
            }

            battleFleet.setTeam(BattleFleetTeam.fromShipTeamBody(fleet.getGuid(), fleet.getFleetBody(), battleFleet.getBattleCommander(), battleFleet.getTechs()));

            result.add(battleFleet);
            // * Fleet changes
            fleet.setMatch(true);
            fleet.setFleetMatch(FleetMatch.builder()
                    .galaxyId(-1)
                    .matchType(match.getMatchType())
                    .match(match.getId())
                    .build());

            if (match.getMatchType().isVirtual()) {
                fleet.setGalaxyId(-1);
            }

        }

        return result;

    }


    public List<BattleFleet> createBattleFleets(List<Fleet> realFleets, InstanceData data, InstanceMatch match) {

        List<BattleFleet> fleets = new ArrayList<>();
        List<PlayerFleetMeta> ally = data.getPlayerFleets();

        Map<Integer, ShipTechs> techs = new HashMap<>();

        List<ResearchData> allResearchData = new ArrayList<>();

        allResearchData.addAll(ResourceManager.getScience().getWeapons());
        allResearchData.addAll(ResourceManager.getScience().getDefense());

        ShipTechs allShipTechs = new ShipTechs();
        allShipTechs.passData(allResearchData);

        int index = 0;

        for (Fleet fleet : realFleets) {

            Commander commander = fleet.getCommander();

            // * BattleFleet creation
            PlayerFleetMeta meta = null;

            if (match.getType().hasPlayerFormation()) {
                meta = ally.get(index++);
            } else {
                meta = new PlayerFleetMeta(fleet.getPosX(), fleet.getPosY());
            }

            BattleFleet battleFleet = new BattleFleet();

            battleFleet.setName(fleet.getName());
            battleFleet.setDefender(false);
            battleFleet.setBodyId(fleet.getBodyId());
            battleFleet.setPosX(meta.getX());
            battleFleet.setPosY(meta.getY());
            battleFleet.setDirection(fleet.getDirection());
            battleFleet.setHe3(fleet.getHe3());
            battleFleet.setMaxHe3(fleet.getMaxHe3());
            battleFleet.setMaxRounds(10);
            battleFleet.setJoinRound(0);
            battleFleet.setGuid(fleet.getGuid());
            battleFleet.setShipTeamId(fleet.getShipTeamId());
            battleFleet.setTarget(fleet.getRangeType());
            battleFleet.setTargetInterval(fleet.getPreferenceType());
            battleFleet.setBattleCommander(commander.createBattleCommander(fleet.getAdditionalGrowth(), null));

            if (fleet.isForceTechs()) {
                battleFleet.setTechs(allShipTechs);
            } else {
                if (techs.containsKey(fleet.getGuid())) {
                    battleFleet.setTechs(techs.get(fleet.getGuid()));
                } else {
                    User user = fleet.getUser();
                    if (user != null) {
                        techs.put(fleet.getGuid(), new ShipTechs(user.getTechs().getTechs()));
                    } else {
                        techs.put(fleet.getGuid(), new ShipTechs(new ArrayList<>()));
                    }
                    battleFleet.setTechs(techs.get(fleet.getGuid()));
                }
            }

            battleFleet.setTeam(BattleFleetTeam.fromShipTeamBody(fleet.getGuid(), fleet.getFleetBody(), battleFleet.getBattleCommander(), battleFleet.getTechs()));

            fleets.add(battleFleet);

            // * Fleet changes
            fleet.setMatch(true);
            fleet.setFleetMatch(FleetMatch.builder()
                    .galaxyId(-1)
                    .matchType(match.getMatchType())
                    .match(match.getId())
                    .build());

            if (fleet.getGalaxyId() != -1) {

                ResponseDeleteShipTeamBroadcastPacket response = new ResponseDeleteShipTeamBroadcastPacket();

                response.setGalaxyMapId(0);
                response.setGalaxyId(fleet.getGalaxyId());
                response.setShipTeamId(fleet.getShipTeamId());

                for (LoggedGameUser loggedGameUser : LoginService.getInstance().getPlanetViewers(fleet.getGalaxyId())) {
                    loggedGameUser.getSmartServer().send(response);
                }

            }

            fleet.setGalaxyId(-1);
            fleet.save();

            // * Commander update
            commander.save();

        }

        return fleets;

    }

    public List<Fleet> getRealFleets(User user, List<Integer> ids) {

        List<Fleet> fleets = new ArrayList<>();

        for (int id : ids) {

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(id);
            if (fleet == null || fleet.getGuid() != user.getGuid()) {
                return null;
            }

            fleets.add(fleet);

        }

        return fleets;

    }

    public List<Fleet> getCopyFleets(User user, List<Integer> ids) {

        List<Fleet> fleets = new ArrayList<>();

        for (int id : ids) {

            Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(id);
            if (fleet == null || fleet.getGuid() != user.getGuid()) {
                return null;
            }

            fleets.add(SerializationUtils.clone(fleet));

        }

        return fleets;

    }

    public int countShips(List<Fleet> fleets) {

        int ships = 0;

        for (Fleet fleet : fleets) {
            for (ShipTeamNum teamNum : fleet.getFleetBody().getCells()) {
                ships += teamNum.getNum();
            }
        }

        return ships;

    }

    public Optional<WarMatch> getWar(Planet planet) {

        return getWars().stream().filter(warMatch -> warMatch.getTargetPlanet().getPosition().equals(planet.getPosition())).findAny();
    }

    public boolean isInWar(Planet planet) {

        return getWars().stream().anyMatch(warMatch -> warMatch.getTargetPlanet().getPosition().equals(planet.getPosition()));
    }

    public LinkedList<Packet> getWarJoinPackets(WarMatch current, User requester, ResponseBuildInfoPacket buildInfoPacket) {

        LinkedList<Packet> packets = new LinkedList<>();

        List<BattleFort> battleFortsList = current.getForts();
        List<BattleFleet> battleFleetList = current.getFleetsSorted();

        ResponseGalaxyShipPacket response = null;
        Planet planet = current.getTargetPlanet();

        for (Fleet fleet : PacketService.getInstance().getFleetCache().findAllByGalaxyIdAndMatch(planet.getPosition().galaxyId(), false)) {

            if (fleet.getFleetTransmission() != null) {
                continue;
            }

            if (response == null) {

                response = new ResponseGalaxyShipPacket();
                response.setGalaxyId(planet.getPosition().galaxyId());
                response.setGalaxyMapId((short) 0);
                response.setFleets(new ArrayList<>());

            } else if (response.getFleets().size() == 189) {

                packets.add(response);
                response = new ResponseGalaxyShipPacket();
                response.setGalaxyId(planet.getPosition().galaxyId());
                response.setGalaxyMapId((short) 0);
                response.setFleets(new ArrayList<>());

            }

            GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

            fleetInfo.setShipTeamId(fleet.getShipTeamId());
            fleetInfo.setShipNum(fleet.ships());
            fleetInfo.setBodyId((short) fleet.getBodyId());
            fleetInfo.setReserve((short) 0);
            fleetInfo.setDirection((byte) fleet.getDirection());

            fleetInfo.setPosX((byte) fleet.getPosX());
            fleetInfo.setPosY((byte) fleet.getPosY());
            fleetInfo.setOwner((byte) (BattleService.getInstance().getFleetColor(requester, fleet)));

            response.getFleets().add(fleetInfo);
            response.setDataLen((short) response.getFleets().size());

        }

        for (BattleFleet battleFleet : battleFleetList) {

            if (response == null) {

                response = new ResponseGalaxyShipPacket();
                response.setGalaxyId(planet.getPosition().galaxyId());
                response.setGalaxyMapId((short) 0);
                response.setFleets(new ArrayList<>());

            } else if (response.getFleets().size() == 189) {

                packets.add(response);
                response = new ResponseGalaxyShipPacket();
                response.setGalaxyId(planet.getPosition().galaxyId());
                response.setGalaxyMapId((short) 0);
                response.setFleets(new ArrayList<>());

            }

            GalaxyFleetInfo fleetInfo = GalaxyFleetInfo.builder()
                    .shipTeamId(battleFleet.getShipTeamId())
                    .shipNum(battleFleet.ships())
                    .bodyId((short) battleFleet.getBodyId())
                    .reserve((short) 1)
                    .direction((byte) battleFleet.getDirection())
                    .posX((byte) battleFleet.getPosX())
                    .posY((byte) battleFleet.getPosY())
                    .owner((byte) (getFleetColor(requester, battleFleet))) // 0 = Attacker (Red), 2 = Mine (Magenta), 3 = Defender (Blue)
                    .build();

            response.getFleets().add(fleetInfo);
            response.setDataLen((short) response.getFleets().size());

        }

        ResponseFightBoutBegPacket responseFightBoutBegPacket = new ResponseFightBoutBegPacket();
        responseFightBoutBegPacket.setGalaxyMapId(-1);
        responseFightBoutBegPacket.setGalaxyId(-1);
        responseFightBoutBegPacket.setBoutId((short) (Math.max(current.getRound(), 0)));
        packets.addFirst(responseFightBoutBegPacket);

        if (response != null) {
            packets.addFirst(response);
        }

        List<BuildInfo> buildInfos = buildInfoPacket.getBuildInfoList();
        ResponseFightInitBuildPacket responseFightInitBuildPacket = null;

        for (BattleFort battleFort : battleFortsList) {

            if (responseFightInitBuildPacket == null) {
                responseFightInitBuildPacket = new ResponseFightInitBuildPacket();
                responseFightInitBuildPacket.setGalaxyId(planet.getPosition().galaxyId());
            } else if (responseFightInitBuildPacket.getFightInitBuilds().size() == 80) {
                packets.addFirst(responseFightInitBuildPacket);
                responseFightInitBuildPacket = new ResponseFightInitBuildPacket();
                responseFightInitBuildPacket.setGalaxyId(planet.getPosition().galaxyId());
            }

            if (battleFort.isDestroyed() && buildInfos.stream().anyMatch(build -> build.getIndexId() == battleFort.getFortId())) {

                buildInfos.remove(buildInfos.stream().filter(build -> build.getIndexId() == battleFort.getFortId()).findAny().get());
                buildInfoPacket.setDataLen(buildInfos.size());
                continue;

            }

            FightInitBuild fightInitBuild = new FightInitBuild(battleFort.getMaxHealth(), battleFort.getHealth(), battleFort.getFortId(), (char) 0, (char) 0);

            responseFightInitBuildPacket.getFightInitBuilds().add(fightInitBuild);
            responseFightInitBuildPacket.setDataLen((short) responseFightInitBuildPacket.getFightInitBuilds().size());

        }

        if (responseFightInitBuildPacket != null) {
            packets.addFirst(responseFightInitBuildPacket);
        }

        // ResponseMatchInfoPacket responseMatchInfoPacket = new ResponseMatchInfoPacket();
        // packets.addFirst(responseMatchInfoPacket);

        // ? First send MatchInfo
        // ? then BoutBeg
        // ? and then fleets
        requester.getLoggedGameUser().get().setMatchViewing(current.getId());
        packets.addFirst(buildInfoPacket);
        return packets;

    }

    public int getFleetColor(User requester, Fleet fleet) {

        boolean attacker = fleet.getFleetInitiator() != null && fleet.getFleetInitiator().getJumpType() == JumpType.ATTACK;
        return getFleetColor(requester.getGuid(), fleet.getGuid(), attacker);
    }

    public int getFleetColor(LoggedGameUser requester, Fleet fleet) {

        boolean attacker = fleet.getFleetInitiator() != null && fleet.getFleetInitiator().getJumpType() == JumpType.ATTACK;
        return getFleetColor(requester.getGuid(), fleet.getGuid(), attacker);
    }

    public int getFleetColor(LoggedGameUser requester, BattleFleet battleFleet) {

        return getFleetColor(requester.getGuid(), battleFleet.getGuid(), battleFleet.isAttacker());
    }

    public int getFleetColor(User requester, BattleFleet battleFleet) {

        return getFleetColor(requester.getGuid(), battleFleet.getGuid(), battleFleet.isAttacker());
    }

    public int getFleetColor(int userGuid, int fleetGuid, boolean attacker) {

        return userGuid == fleetGuid ? 2 : (attacker ? 1 : 0);
    }

    public List<ResponseFightInitShipTeamPacket> getFightInitShipTeamPackets(Match match, BattleFleet battleFleet) {

        List<ResponseFightInitShipTeamPacket> packets = new ArrayList<>();

        ResponseFightInitShipTeamPacket shipTeamPacket = new ResponseFightInitShipTeamPacket();

        shipTeamPacket.setGalaxyMapId(0);
        shipTeamPacket.setGalaxyId(match.getGalaxyId());
        shipTeamPacket.setFleets(new ArrayList<>());

        // Attacker fleet
        shipTeamPacket.getFleets().add(getFightInitShipTeam(battleFleet)); // default attack display direction

        // Trash
        shipTeamPacket.getFleets().add(getEmptyFightInitShipTeam());

        shipTeamPacket.setDataLen(shipTeamPacket.getFleets().size());
        packets.add(shipTeamPacket);

        return packets;

    }

    public List<ResponseFightInitShipTeamPacket> getFightInitShipTeamPackets(Match match, BattleFleet attackerFleet, BattleFleet defenderFleet, int defensiveDirection) {

        List<ResponseFightInitShipTeamPacket> packets = new ArrayList<>();

        ResponseFightInitShipTeamPacket shipTeamPacket = new ResponseFightInitShipTeamPacket();

        shipTeamPacket.setGalaxyMapId(0);
        shipTeamPacket.setGalaxyId(match.getGalaxyId());
        shipTeamPacket.setFleets(new ArrayList<>());

        // Attacker fleet
        shipTeamPacket.getFleets().add(getFightInitShipTeam(attackerFleet)); // default attack display direction

        // Defender fleet
        shipTeamPacket.getFleets().add(getFightInitShipTeam(defenderFleet)); // default defensive direction

        shipTeamPacket.setDataLen(shipTeamPacket.getFleets().size());
        packets.add(shipTeamPacket);

        return packets;

    }

    public FightInitShipTeamInfo getEmptyFightInitShipTeam() {

        FightInitShipTeamInfo shipTeamInfo = new FightInitShipTeamInfo();

        shipTeamInfo.setShipTeamId(-1);

        shipTeamInfo.setUserId(-1);
        shipTeamInfo.setTeamOwnerName("");
        shipTeamInfo.setCommanderName("");

        for (int i = 0; i < 9; i++) {

            shipTeamInfo.getShield().set(i, 0);
            shipTeamInfo.getMaxShield().set(i, 0);

            shipTeamInfo.getEndure().set(i, 0);
            shipTeamInfo.getMaxEndure().set(i, 0);

        }

        shipTeamInfo.setTeamBody(new ShipTeamBody());

        shipTeamInfo.setSkillId(0);
        shipTeamInfo.setReserve(0);

        shipTeamInfo.setAttackObjInterval(0);
        shipTeamInfo.setAttackObjType(0);

        shipTeamInfo.setLevelId(0);
        shipTeamInfo.setCardLevel(0);

        // BotLogger.log(shipTeamInfo);
        return shipTeamInfo;

    }

    public FightInitShipTeamInfo getFightInitShipTeam(BattleFleet battleFleet) {

        BattleFleetTeam battleFleetTeam = battleFleet.getTeam();
        FightInitShipTeamInfo shipTeamInfo = new FightInitShipTeamInfo();

        shipTeamInfo.setShipTeamId(battleFleet.getShipTeamId());

        if (battleFleet.isPirate()) {

            shipTeamInfo.setUserId(-1);
            shipTeamInfo.setTeamOwnerName("Pirate");
            shipTeamInfo.setCommanderName(battleFleet.getBattleCommander().getName());

        } else {

            User user = UserService.getInstance().getUserCache().findByGuid(battleFleet.getGuid());

            shipTeamInfo.setUserId(user.getUserId());
            shipTeamInfo.setTeamOwnerName(user.getUsername());
            shipTeamInfo.setCommanderName(battleFleet.getBattleCommander().getName());

            if (user.getConsortiaId() != -1) {

                Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(user.getConsortiaId());

                if (corp != null) {
                    shipTeamInfo.setConsortiaName(corp.getName());
                }

            }

        }

        for (int i = 0; i < 9; i++) {

            if (battleFleetTeam.getCells().size() > i) {

                BattleFleetCell cell = battleFleetTeam.getCells().get(i);

                shipTeamInfo.getShield().set(i, cell.getShields());
                shipTeamInfo.getMaxShield().set(i, cell.getMaxShields());

                shipTeamInfo.getEndure().set(i, cell.getStructure());
                shipTeamInfo.getMaxEndure().set(i, cell.getMaxStructure());

            }

        }

        shipTeamInfo.setTeamName(battleFleet.getName());

        shipTeamInfo.setGas((int) battleFleet.getHe3());
        shipTeamInfo.setStorage((int) battleFleet.getMaxHe3());
        shipTeamInfo.setTeamBody(battleFleet.getTeam().getTeamBody());

        shipTeamInfo.setSkillId(battleFleet.getBattleCommander().getSkillId());
        shipTeamInfo.setReserve(0);

        shipTeamInfo.setAttackObjInterval(battleFleet.getTarget());
        shipTeamInfo.setAttackObjType(battleFleet.getTargetInterval());

        shipTeamInfo.setLevelId(battleFleet.getBattleCommander().getLevel());
        shipTeamInfo.setCardLevel(battleFleet.getBattleCommander().getStars());

        // BotLogger.log(shipTeamInfo);
        return shipTeamInfo;

    }

    public ResponseFightSectionPacket getEmptyFightSection(Match match) {

        ResponseFightSectionPacket fightSectionPacket = new ResponseFightSectionPacket();

        fightSectionPacket.setGalaxyId(match.getGalaxyId());
        fightSectionPacket.setBoutId((short) match.getRound());

        fightSectionPacket.setSourceShipTeamId(-1);
        fightSectionPacket.setToShipTeamId(-1);

        fightSectionPacket.setSourceRepairSupply(0);
        fightSectionPacket.setGalaxyMapId((byte) 0);
        fightSectionPacket.setSourceDirection((byte) 4);
        fightSectionPacket.setDelFlag((byte) 0);
        fightSectionPacket.setBothStatus((byte) 0);
        fightSectionPacket.setRepelStep((byte) 0);

        fightSectionPacket.setShipFights(new ArrayList<>());

        for (int i = 0; i < 9; i++) {

            ShipFight shipFight = new ShipFight();

            shipFight.setSourceReduceSupply(0);
            shipFight.setTargetReduceSupply(0);

            shipFight.setSourceReduceStorage(0);
            shipFight.setTargetReduceStorage(0);

            shipFight.setSourceReduceHp(0);

            shipFight.setTargetReduceEndure(0);
            shipFight.setSourceReduceShipNum(0);

            shipFight.setSourceSkill(0);
            shipFight.setTargetSkill(0);
            shipFight.setTargetBlast(0);

            fightSectionPacket.getShipFights().add(shipFight);

        }

        int[] movementPath = new int[16];
        Arrays.fill(movementPath, -1);

        fightSectionPacket.setSourceMovePath(new CharArray(movementPath));

        return fightSectionPacket;

    }

    public ResponseFightFortressSectionPacket getEmptyFightFortressSection(Match match) {

        ResponseFightFortressSectionPacket fightFortressSectionPacket = new ResponseFightFortressSectionPacket();

        fightFortressSectionPacket.setGalaxyId(match.getGalaxyId());
        fightFortressSectionPacket.setSourceId(-1);


        fightFortressSectionPacket.setGalaxyMapId((short) 0);
        fightFortressSectionPacket.setBoutId((short) match.getRound());

        fightFortressSectionPacket.setBuildType((byte) 0);
        fightFortressSectionPacket.setReserve1(UnsignedChar.of(0));
        fightFortressSectionPacket.setReserve2(UnsignedChar.of(0));

        fightFortressSectionPacket.setFortressFights(new ArrayList<>());
        fightFortressSectionPacket.setDataLen((byte) fightFortressSectionPacket.getFortressFights().size());
        return fightFortressSectionPacket;

    }

    public ResponseEctypeStatePacket getCurrentEctypeState(User user) {

        return getCurrentEctypeState(user, 2);
    }

    public ResponseEctypeStatePacket getCurrentEctypeState(User user, int state) {

        Optional<Match> optionalCurrent = BattleService.getInstance().getVirtual(user.getGuid());

        ResponseEctypeStatePacket response = new ResponseEctypeStatePacket();
        response.setGateId(UnsignedChar.of(0));
        response.setState((byte) state);

        if (optionalCurrent.isPresent()) {
            response.setEctypeId((short) optionalCurrent.get().getEctype());
        } else {
            response.setEctypeId((short) -1);
        }
        return response;

    }

    public ResponseFightResultPacket getArenaFightResult(ArenaMatch arenaMatch, boolean won) {

        BattleReport report = arenaMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);
        fightResultPacket.setVictory((short) (won ? 0 : 1));

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder()))).toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();

        int attackIndex = 0;
        int commanderIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {

            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {

                if (userCache.containsKey(killerShip.getGuid())) {

                    user = userCache.get(killerShip.getGuid());

                } else {

                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }

                    userCache.put(user.getGuid(), user);

                }

                userId = user.getUserId();
                name = user.getUsername();

            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {

            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());

            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }

            // Pirate
            if (user == null) {

                fightResultPacket.setTopAssaultOwner(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultCommander(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
                fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
                fightResultPacket.setTopAssaultUserId(0);
                fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
                return fightResultPacket;

            }

            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());

        }

        for (BattleExpCache expCache : report.getExpHistoric()) {

            if (commanderIndex >= 9) {
                break;
            }

            User user = UserService.getInstance().getUserCache().findByGuid(expCache.getGuid());
            if (user == null) {
                continue;
            }

            Commander commander = CommanderService.getInstance().getCommanderCache().findByCommanderId(expCache.getCommanderId());
            if (commander == null) {
                continue;
            }

            FightTotalExp fightTotalExp = new FightTotalExp();

            fightTotalExp.setUserId(user.getUserId());
            fightTotalExp.setCommanderUserId(user.getUserId());
            fightTotalExp.setCommanderName(commander.getName());
            fightTotalExp.setHeadId(commander.getSkill());
            fightTotalExp.setExp(expCache.getExp());
            fightTotalExp.setLevelId(expCache.getLevelId());
            fightTotalExp.setRoleName(user.getUsername());

            fightResultPacket.getExp().add(fightTotalExp);
            commanderIndex++;

        }

        return fightResultPacket;

    }

    public ResponseFightResultPacket getLeagueFightResult(LeagueMatch leagueMatch, BattleResultType result) {

        BattleReport report = leagueMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);
        if (BattleResultType.WIN.equals(result)) {
            fightResultPacket.setVictory((short) 0);
        } else if (BattleResultType.LOSE.equals(result)) {
            fightResultPacket.setVictory((short) 1);
        } else if (BattleResultType.DRAW.equals(result)) {
            fightResultPacket.setVictory((short) 2);
        }

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder()))).toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();

        int attackIndex = 0;
        int commanderIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {

            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {

                if (userCache.containsKey(killerShip.getGuid())) {

                    user = userCache.get(killerShip.getGuid());

                } else {

                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }

                    userCache.put(user.getGuid(), user);

                }

                userId = user.getUserId();
                name = user.getUsername();

            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {

            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());

            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }

            // Pirate
            if (user == null) {

                fightResultPacket.setTopAssaultOwner(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultCommander(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
                fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
                fightResultPacket.setTopAssaultUserId(0);
                fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
                return fightResultPacket;

            }

            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());

        }

        for (BattleExpCache expCache : report.getExpHistoric()) {

            if (commanderIndex >= 9) {
                break;
            }

            User user = UserService.getInstance().getUserCache().findByGuid(expCache.getGuid());
            if (user == null) {
                continue;
            }

            Commander commander = CommanderService.getInstance().getCommanderCache().findByCommanderId(expCache.getCommanderId());
            if (commander == null) {
                continue;
            }

            FightTotalExp fightTotalExp = new FightTotalExp();

            fightTotalExp.setUserId(user.getUserId());
            fightTotalExp.setCommanderUserId(user.getUserId());
            fightTotalExp.setCommanderName(commander.getName());
            fightTotalExp.setHeadId(commander.getSkill());
            fightTotalExp.setExp(expCache.getExp());
            fightTotalExp.setLevelId(expCache.getLevelId());
            fightTotalExp.setRoleName(user.getUsername());

            fightResultPacket.getExp().add(fightTotalExp);
            commanderIndex++;

        }

        return fightResultPacket;

    }
    public ResponseFightResultPacket getRaidFightResult(RaidMatch raidMatch, BattleResultType result) {

        BattleReport report = raidMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);
        if (BattleResultType.WIN.equals(result)) {
            fightResultPacket.setVictory((short) 0);
        } else if (BattleResultType.LOSE.equals(result)) {
            fightResultPacket.setVictory((short) 1);
        } else if (BattleResultType.DRAW.equals(result)) {
            fightResultPacket.setVictory((short) 2);
        }

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder()))).toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();

        int attackIndex = 0;
        int commanderIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {

            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {
                if (userCache.containsKey(killerShip.getGuid())) {
                    user = userCache.get(killerShip.getGuid());
                } else {
                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }
                    userCache.put(user.getGuid(), user);
                }
                userId = user.getUserId();
                name = user.getUsername();
            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {

            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());

            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }

            // Pirate
            if (user == null) {
                fightResultPacket.setTopAssaultOwner(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultCommander(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
                fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
                fightResultPacket.setTopAssaultUserId(0);
                fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
                return fightResultPacket;

            }

            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());

        }

        return fightResultPacket;

    }

    public ResponseFightResultPacket getChampFightResult(ChampMatch champMatch, BattleResultType result) {

        BattleReport report = champMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);
        if (BattleResultType.WIN.equals(result)) {
            fightResultPacket.setVictory((short) 0);
        } else if (BattleResultType.LOSE.equals(result)) {
            fightResultPacket.setVictory((short) 1);
        } else if (BattleResultType.DRAW.equals(result)) {
            fightResultPacket.setVictory((short) 2);
        }

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder())))
                .toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();

        int attackIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {

            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {

                if (userCache.containsKey(killerShip.getGuid())) {

                    user = userCache.get(killerShip.getGuid());

                } else {

                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }

                    userCache.put(user.getGuid(), user);

                }

                userId = user.getUserId();
                name = user.getUsername();

            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {

            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());

            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }
            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
        }

        return fightResultPacket;

    }

    public ResponseFightResultPacket getWarFightResult(WarMatch warMatch, BattleResultType resultType) {

        BattleReport report = warMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);

        switch (resultType) {
            case WIN -> fightResultPacket.setVictory((short) 0);
            case LOSE -> fightResultPacket.setVictory((short) 1);
            case DRAW -> fightResultPacket.setVictory((short) 2);
        }

        for (int i = 0; i < report.getFightRobResources().size(); i++) {

            if (i == 10) {
                break;
            }
            fightResultPacket.getPrize().add(report.getFightRobResources().get(i));

        }

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder()))).toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();

        int attackIndex = 0;
        int commanderIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {

            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {

                if (userCache.containsKey(killerShip.getGuid())) {

                    user = userCache.get(killerShip.getGuid());

                } else {

                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }

                    userCache.put(user.getGuid(), user);

                }

                userId = user.getUserId();
                name = user.getUsername();

            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {

            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());

            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }

            // Pirate
            if (user == null) {

                fightResultPacket.setTopAssaultOwner(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultCommander(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
                fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
                fightResultPacket.setTopAssaultUserId(0);
                fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
                return fightResultPacket;

            }

            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());

        }

        for (BattleExpCache expCache : report.getExpHistoric()) {

            if (commanderIndex >= 9) {
                break;
            }

            User user = UserService.getInstance().getUserCache().findByGuid(expCache.getGuid());
            if (user == null) {
                continue;
            }

            Commander commander = CommanderService.getInstance().getCommanderCache().findByCommanderId(expCache.getCommanderId());
            if (commander == null) {
                continue;
            }

            FightTotalExp fightTotalExp = new FightTotalExp();

            fightTotalExp.setUserId(user.getUserId());
            fightTotalExp.setCommanderUserId(user.getUserId());
            fightTotalExp.setCommanderName(commander.getName());
            fightTotalExp.setHeadId(commander.getSkill());
            fightTotalExp.setExp(expCache.getExp());
            fightTotalExp.setLevelId(expCache.getLevelId());
            fightTotalExp.setRoleName(user.getUsername());

            fightResultPacket.getExp().add(fightTotalExp);
            commanderIndex++;

        }

        return fightResultPacket;

    }

    public ResponseFightResultPacket getInstanceFightResult(InstanceMatch instanceMatch, boolean won) {

        BattleReport report = instanceMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);
        fightResultPacket.setVictory((short) (won ? 1 : 0));

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder())))
                .toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();

        int attackIndex = 0;
        int commanderIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {

            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {

                if (userCache.containsKey(killerShip.getGuid())) {

                    user = userCache.get(killerShip.getGuid());

                } else {

                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }

                    userCache.put(user.getGuid(), user);

                }

                userId = user.getUserId();
                name = user.getUsername();

            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {

            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());

            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }

            // Pirate
            if (user == null) {

                fightResultPacket.setTopAssaultOwner(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultCommander(SmartString.of("Pirate", 32));
                fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
                fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
                fightResultPacket.setTopAssaultUserId(0);
                fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
                return fightResultPacket;

            }

            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());

        }

        for (BattleExpCache expCache : report.getExpHistoric()) {

            if (commanderIndex >= 9) {
                break;
            }

            User user = UserService.getInstance().getUserCache().findByGuid(expCache.getGuid());
            if (user == null) {
                continue;
            }

            Commander commander = CommanderService.getInstance().getCommanderCache().findByCommanderId(expCache.getCommanderId());
            if (commander == null) {
                continue;
            }

            FightTotalExp fightTotalExp = new FightTotalExp();

            fightTotalExp.setUserId(user.getUserId());
            fightTotalExp.setCommanderUserId(user.getUserId());
            fightTotalExp.setCommanderName(commander.getName());
            fightTotalExp.setHeadId(commander.getSkill());
            fightTotalExp.setExp(expCache.getExp());
            fightTotalExp.setLevelId(expCache.getLevelId());
            fightTotalExp.setRoleName(user.getUsername());

            fightResultPacket.getExp().add(fightTotalExp);
            commanderIndex++;

        }

        return fightResultPacket;

    }

    public static int getDirection(GO2Node from, GO2Node to) {

        return getDirection(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static int getSmartDirection(GO2Node from, GO2Node to, int current) {

        int relativeX = to.getX() - from.getX();
        int relativeY = to.getY() - from.getY();

        if (relativeY >= relativeX && relativeY < -relativeX) {
            return 2;
        }

        if (relativeY > relativeX && relativeY >= -relativeX) {
            return 1;
        }

        if (relativeY < relativeX && relativeY <= -relativeX) {
            return 3;
        }

        if (relativeY <= relativeX && relativeY > -relativeX) {
            return 0;
        }

        return current;
    }

    public static int getDirection(Node from, Node to) {

        return getDirection(from.getX(), from.getY(), to.getX(), to.getY());
    }

    public static int getDirection(int fromX, int fromY, int toX, int toY) {

        if (fromX < toX) {
            return 0;
        }

        if (fromY < toY) {
            return 1;
        }

        if (fromX > toX) {
            return 2;
        }

        if (fromY > toY) {
            return 3;
        }

        // Nunca va a pasar
        return 0; // todo check??

    }

    public static int getTarget(String name) {

        if ("target:maxRange".equals(name)) {
            return 1;
        }
        return 0;
    }

    public static int getTargetInterval(String name) {

        return switch (name) {
            case "target:commander" -> 1;
            case "target:maxAttack" -> 2;
            case "target:minAttack" -> 3;
            case "target:maxDurability" -> 4;
            case "target:minDurability" -> 5;
            case "target:defBuildings" -> 6;
            default -> 0;
        };
    }

    public static BattleService getInstance() {

        return instance;
    }

    public ResponseFightResultPacket getIglFightResult(IglMatch iglMatch, BattleResultType resultType) {
        BattleReport report = iglMatch.getBattleReport();
        ResponseFightResultPacket fightResultPacket = new ResponseFightResultPacket();

        fightResultPacket.setKind((short) 0);
        fightResultPacket.setGalaxyId(-1);

        fightResultPacket.setTopAssaultUserId(0);
        if (BattleResultType.WIN.equals(resultType)) {
            fightResultPacket.setVictory((short) 1);
        } else if (BattleResultType.LOSE.equals(resultType)) {
            fightResultPacket.setVictory((short) 0);
        } else if (BattleResultType.DRAW.equals(resultType)) {
            fightResultPacket.setVictory((short) 2);
        }

        fightResultPacket.setAttackShipNumber(report.getTotalAttackerSent());
        fightResultPacket.setDefendShipNumber(report.getTotalDefenderSent());

        fightResultPacket.setAttackLossNumber(report.getTotalAttackerLost());
        fightResultPacket.setDefendLossNumber(report.getTotalDefenderLost());

        List<BattleShipCache> fallenShips = report.getShipHistoric().stream()
                .sorted(Comparator.comparing(BattleShipCache::getShootdowns, Comparator.nullsFirst(Comparator.reverseOrder())))
                .toList();

        Optional<BattleShipCache> optionalHighestAttack = report.getShipHistoric().stream().min(Comparator.comparing(BattleShipCache::getHighestAttack, Comparator.nullsFirst(Comparator.reverseOrder())));

        Map<Integer, User> userCache = new HashMap<>();
        int attackIndex = 0;

        for (BattleShipCache killerShip : fallenShips) {
            if (attackIndex >= 9) {
                break;
            }
            if (killerShip.getShootdowns() == 0) {
                continue;
            }

            User user;
            String name = "Pirate";
            long userId = 0;

            if (killerShip.getGuid() != -1) {
                if (userCache.containsKey(killerShip.getGuid())) {
                    user = userCache.get(killerShip.getGuid());
                } else {
                    user = UserService.getInstance().getUserCache().findByGuid(killerShip.getGuid());
                    if (user == null) {
                        continue;
                    }
                    userCache.put(user.getGuid(), user);
                }
                userId = user.getUserId();
                name = user.getUsername();
            }

            ShipModel shipModel = PacketService.getShipModel(killerShip.getShipModelId());
            if (shipModel == null) {
                continue;
            }

            FightTotalKill fightTotalKill = new FightTotalKill();

            fightTotalKill.setUserId(userId);
            fightTotalKill.setNum((int) killerShip.getShootdowns());

            fightTotalKill.setBodyId(shipModel.getBodyId());
            fightTotalKill.setReserve(0);

            fightTotalKill.setModelName(shipModel.getName());
            fightTotalKill.setRoleName(name);

            fightResultPacket.getKill().add(fightTotalKill);
            attackIndex++;

        }

        if (optionalHighestAttack.isPresent()) {
            BattleShipCache highestAttack = optionalHighestAttack.get();
            User user = UserService.getInstance().getUserCache().findByGuid(highestAttack.getGuid());
            ShipModel shipModel = PacketService.getShipModel(highestAttack.getShipModelId());
            if (shipModel == null) {
                return fightResultPacket;
            }
            fightResultPacket.setTopAssaultOwner(SmartString.of(user.getUsername(), 32));
            fightResultPacket.setTopAssaultCommander(SmartString.of("Commander", 32));
            fightResultPacket.setTopAssaultBodyId((short) shipModel.getBodyId());
            fightResultPacket.setTopAssaultModelName(SmartString.of(shipModel.getName(), 32));
            fightResultPacket.setTopAssaultUserId(user.getUserId());
            fightResultPacket.setTopAssaultValue((int) highestAttack.getHighestAttack());
        }


        return fightResultPacket;
    }
}

package com.go2super.service.champ;

import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.UserChampStats;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.type.MatchType;
import com.go2super.logger.BotLogger;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.fight.ResponseWarfieldStatusPacket;
import com.go2super.packet.rank.WarFieldPage;
import com.go2super.service.BattleService;
import com.go2super.service.UserService;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.match.ChampMatch;
import com.go2super.service.battle.type.StopCause;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Getter
@Service
@EnableScheduling
public class ChampService {

    @Getter
    private static ChampService instance;

    private static List<ChampPhase> phases = List.of(
            new ChampPhase(
                    DayOfWeek.FRIDAY,
                    LocalTime.of(20, 0, 0), // register start
                    DayOfWeek.SATURDAY,
                    LocalTime.of(11, 59, 59), // register end
                    DayOfWeek.SATURDAY,
                    LocalTime.of(12, 0, 0), // match start
                    LocalTime.of(23, 59, 59), // match end
                    ChampPhase.Phase.QUALIFICATION,
                    900

            ),
            new ChampPhase(
                    DayOfWeek.SUNDAY,
                    LocalTime.of(0, 0, 0), // register start
                    DayOfWeek.SUNDAY,
                    LocalTime.of(11, 59, 59), // register end
                    DayOfWeek.SUNDAY,
                    LocalTime.of(12, 0, 0), // match start
                    LocalTime.of(23, 59, 59), // match end
                    ChampPhase.Phase.FINAL,
                    60
            )
    );


    private static final int MAX_FLEET_COUNT = 8;
    private static ChampMatch WAITING_ROOM = BattleService.getInstance().makeChampsWaitingRoom();
    @Getter
    private final UserCache userCache;

    private List<List<WarFieldPage>> rankList = new ArrayList<>();

    @Autowired
    public ChampService(UserCache userCache) {
        instance = this;
        this.userCache = userCache;
        setup();
    }

    @Scheduled(cron = "0 0 * * * *")
    public void setup() {
        List<WarFieldPage> initialUsers = userCache.findAll().stream().map(x -> {
                    WarFieldPage page = new WarFieldPage();
                    page.setUserId(x.getUserId());
                    page.setName(x.getUsername());
                    page.setGuid(x.getGuid());
                    UserChampStats champStats = x.getStats().getUserChampStats();
                    page.setWarScore(champStats.getPoints());
                    page.setWarKilldown(champStats.getShootdowns());
                    page.setWarWin(champStats.getWins());
                    return page;
                }).sorted(Comparator.comparing(WarFieldPage::getWarScore).reversed())
                .collect(Collectors.toList());
        rankList = partitionList(initialUsers, 6);
    }

    public int pageSize() {
        return rankList.size();
    }


    public Pair<Integer, List<WarFieldPage>> getPageByGuid(long guid) {
        for (int i = 0; i < rankList.size(); i++) {
            List<WarFieldPage> pages = rankList.get(i);
            for (int j = 0; j < pages.size(); j++) {
                WarFieldPage page = pages.get(j);
                if (page.getGuid() == guid) {
                    return Pair.of(i, pages);
                }
            }
        }
        return Pair.of(0, Collections.emptyList());

    }

    public List<WarFieldPage> getByPage(int page) {
        return rankList.get(page);
    }

    public synchronized boolean addPlayer(Integer id, List<Integer> fleetIds) {
        ChampPhase activePhase = getActivePhase();
        if (activePhase == null || !activePhase.isRegistrationTime() || activePhase.maxUserCount() <= WAITING_ROOM.getTargetIds().size()) {
            return false;
        }

        if (WAITING_ROOM.hasUser(id)) {
            return false;
        }

        User player1 = UserService.getInstance().getUserCache().findByUserId(id);
        if (activePhase.phase() == ChampPhase.Phase.FINAL) {
            UserInventory inventory = player1.getInventory();
            boolean hasProp = inventory.hasProp(4424, 1, 0, true);
            if (!hasProp) {
                return false;
            }
            inventory.removeOneProp(inventory.getProp(4424), true);
        }

        if (fleetIds == null || fleetIds.isEmpty() || fleetIds.size() > MAX_FLEET_COUNT) {
            return false;
        }

        WAITING_ROOM.addPlayer(player1, fleetIds, true);
        return true;
    }

    public synchronized boolean removePlayer(Integer id) {
        return WAITING_ROOM.removePlayer(id);
    }

    public Integer getWaitingRoomSize() {
        return WAITING_ROOM.getTargetIds().size();
    }

    public boolean isPlayerInChampWaitingRoom(Integer id) {
        return WAITING_ROOM.hasUser(id);
    }

    public boolean isPlayerInChampMatch(Integer id) {
        return BattleService.getInstance().findChampMatchByGuid(id).isPresent();
    }

    private static <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();

        for (int i = 0; i < list.size(); i += size) {
            int end = Math.min(i + size, list.size());
            partitions.add(new ArrayList<>(list.subList(i, end)));
        }

        return partitions;
    }

    @Scheduled(cron = "0 0 12 * * SAT,SUN")
    private void startChamps() {
        BotLogger.info("Starting champs");

        LinkedList<PlayerChamp> players = WAITING_ROOM.getTargetIds().stream()
                .map(id -> {
                    List<Integer> fleetIds = WAITING_ROOM.getFleets()
                            .stream()
                            .filter(x -> x.getGuid() == id)
                            .map(BattleFleet::getShipTeamId)
                            .collect(Collectors.toList());
                    return new PlayerChamp(id, fleetIds);
                })
                .collect(Collectors.toCollection(LinkedList::new));
        Collections.shuffle(players);
        WAITING_ROOM.stop(StopCause.MANUAL);

        ChampPhase activePhase = getActivePhase();
        int totalPlayers = players.size();
        int roomSize = 60;

        if (activePhase.phase() == ChampPhase.Phase.QUALIFICATION) {
            int desiredMinimumRoomSize = 12;
            int maxRooms = 15;
            int roomsNeeded = (int) Math.ceil((double) totalPlayers / desiredMinimumRoomSize);
            if (roomsNeeded > maxRooms) {
                roomsNeeded = maxRooms;
            }
            roomSize = (int) Math.ceil((double) totalPlayers / roomsNeeded);
        }

        List<List<PlayerChamp>> partition = partitionList(players, roomSize);

        // check if a room only contains 1 player and merge it with the first room
        for (int i = 1; i < partition.size(); i++) {
            List<PlayerChamp> room = partition.get(i);
            if (room.size() == 1) {
                partition.get(i - 1).addAll(room);
                partition.remove(i);
                i--;
            }
        }


        int maskId = 0;
        boolean isFinals = activePhase.phase() == ChampPhase.Phase.FINAL;
        List<MatchRunnable> rooms = new ArrayList<>();
        for (int i = 0; i < partition.size(); i++) {
            List<PlayerChamp> room = partition.get(i);
            List<PlayerChamp> attackerSide = room.subList(0, room.size() / 2);
            List<PlayerChamp> defenderSide = room.subList(room.size() / 2, room.size());
            MatchRunnable runnable = BattleService.getInstance().makeChampMatch(i, activePhase.phase());
            ChampMatch match = (ChampMatch) runnable.getMatch();
            maskId |= 1 << i + 1;
            match.setRoomMaskId(isFinals ? 1 : maskId);

            for (PlayerChamp player : attackerSide) {
                User u = UserService.getInstance().getUserCache().findByUserId(player.userId());
                match.addPlayer(u, player.fleets(), true);
            }

            for (PlayerChamp player : defenderSide) {
                User u = UserService.getInstance().getUserCache().findByUserId(player.userId());
                match.addPlayer(u, player.fleets(), false);
            }
            rooms.add(runnable);
        }

        for (MatchRunnable room : rooms) {
            ChampMatch match = (ChampMatch) room.getMatch();
            BotLogger.info("Starting champ match " + match.getRoomId());
            BotLogger.info("Starting champ match with " + match.getSourceIds().size() + " attackers");
            BotLogger.info("Starting champ match with " + match.getTargetIds().size() + " defenders");
            boolean started = match.start();
            if (!started) {
                BotLogger.error("Failed to start champ match: " + match.getRoomId());
            }
        }

        final Integer waitingRoomSize = ChampService.getInstance().getWaitingRoomSize();
        final int maskIdFinal = maskId;
        for (User x : UserService.getInstance().getUserCache().findAll()) {
            Optional<LoggedGameUser> loggedGameUser = x.getLoggedGameUser();
            loggedGameUser.ifPresent(gameUser -> {
                ResponseWarfieldStatusPacket response = new ResponseWarfieldStatusPacket();
                response.setUserNumber(UnsignedShort.of(waitingRoomSize));
                response.setStatus((byte) 0);
                response.setMatchLevel((byte) x.getCurrentLeague());
                response.setWarfield(isFinals ? 1 : maskIdFinal);
                gameUser.getSmartServer().send(response);
            });
        }
        WAITING_ROOM = BattleService.getInstance().makeChampsWaitingRoom();
    }

    @Scheduled(cron = "0 0 0 * * SUN,MON")
    private void endChamps() {
        BotLogger.info("Ending champs");
        BattleService.getInstance().endActiveChampMatches();
    }

    public ChampPhase getActivePhase() {
        return phases.stream()
                .filter(ChampPhase::isActive)
                .findFirst()
                .orElse(null);
    }

    public int getCurrentWarFieldBitMask() {
        List<GameBattle> battles = BattleService.getInstance().getBattles(MatchType.CHAMPION_MATCH);
        if (battles.size() == 1) {
            ChampMatch match = (ChampMatch) battles.get(0).getMatch();
            if (match.getPhase() == ChampPhase.Phase.FINAL) {
                return 1;
            }
        }
        int mask = 0;
        for (GameBattle battle : battles) {
            ChampMatch match = (ChampMatch) battle.getMatch();
            mask |= match.getRoomMaskId();
        }
        return mask;
    }


}

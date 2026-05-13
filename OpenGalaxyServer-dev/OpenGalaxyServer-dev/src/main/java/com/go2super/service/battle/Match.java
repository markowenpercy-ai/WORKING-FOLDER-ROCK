package com.go2super.service.battle;

import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.MatchType;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.Packet;
import com.go2super.service.LoginService;
import com.go2super.service.battle.type.AttackSideType;
import com.go2super.service.battle.type.StopCause;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class Match {

    private String id;

    private MatchType matchType;
    private long startDate;

    private int galaxyId = -1;
    private int ectype = -1;

    private int round = -1;
    private int maxRound = -1;
    private int action = 0;
    private int trigger = 0;

    private int movementIndex = 0;
    private int index = 0;

    private long lastAction = 0L;
    private long estimatedDuration = 0L;

    private Date startLastAction;
    private Date endLastAction;

    private BattleReport battleReport = new BattleReport();

    private final AtomicBoolean pause = new AtomicBoolean(true);
    private boolean ended;

    private CopyOnWriteArrayList<BattleFleet> fleets = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<BattleFort> forts = new CopyOnWriteArrayList<>();

    private BattleRound currentRound;
    private BattleAction battleAction;
    private BattleMetadata metadata;
    private ConcurrentLinkedQueue<Packet> packets = new ConcurrentLinkedQueue<>();
    private boolean watchMode;


    public abstract void stop(StopCause cause);

    public abstract void updateFleet(BattleFleet battleFleet);

    public abstract void removeFleet(BattleFleet battleFleet);

    public abstract void removeFort(BattleFort battleFort);

    public abstract void returnAllFleets();

    public abstract AttackSideType fortressAttackType();

    public abstract boolean hasUser(int guid);

    public List<BattleElement> getTemporalElementsSorted() {

        List<BattleFleet> fleets = getFleetsSorted();
        List<BattleFort> forts = getFortsSorted();

        List<BattleElement> cloned = new ArrayList<>();

        for (BattleFleet fleet : fleets) {
            cloned.add(SerializationUtils.clone(fleet));
        }

        for (BattleFort fort : forts) {
            cloned.add(SerializationUtils.clone(fort));
        }

        return cloned;

    }

    public List<BattleFleet> getTemporalFleetsSorted() {

        List<BattleFleet> fleets = getFleetsSorted();
        List<BattleFleet> cloned = new ArrayList<>();

        for (BattleFleet fleet : fleets) {
            cloned.add(SerializationUtils.clone(fleet));
        }

        return cloned;

    }

    public List<BattleFort> getFortsSorted() {

        List<BattleFort> toRemove = new ArrayList<>();

        for (BattleFort battleFort : getForts()) {
            if (battleFort.isDestroyed()) {
                toRemove.add(battleFort);
            }
        }

        for (BattleFort remove : toRemove) {
            removeFort(remove);
        }

        List<BattleFort> forts = getForts().stream()
            .filter(fort -> !fort.isDestroyed())
            .sorted(Comparator.comparing(BattleFort::getFortId))
            .collect(Collectors.toList());

        return forts;

    }

    public List<BattleFleet> getFleetsSorted() {

        List<BattleFleet> toRemove = new ArrayList<>();

        for (BattleFleet battleFleet : getFleets()) {
            if (battleFleet.isDestroyed()) {
                toRemove.add(battleFleet);
            }
        }

        for (BattleFleet remove : toRemove) {
            removeFleet(remove);
        }

        List<BattleFleet> fleets = getFleets().stream()
            .filter(fleet -> !fleet.isDestroyed())
            .sorted(Comparator.comparingInt(BattleFleet::getShipTeamId).reversed())
            .sorted(Comparator.naturalOrder())
            .collect(Collectors.toList());

        return fleets;

    }

    public BattleFleet getBattleFleetByShipTeamId(int shipTeamId) {

        for (BattleFleet battleFleet : getFleets()) {
            if (battleFleet.getShipTeamId() == shipTeamId) {
                return battleFleet;
            }
        }

        return null;

    }

    public int[][] getBlocks(BattleFleet starter, BattleCell[][] matrix) {

        // BotLogger.log("OBSTACLES WITH: " + starter);
        List<Pair<Integer, Integer>> obstacles = new ArrayList<>();

        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {

                BattleCell current = matrix[i][j];

                if (current == null) {
                    continue;
                }

                if (current.hasEnemies(starter)) {
                    obstacles.add(Pair.of(i, j));
                }

            }
        }

        int[][] blocks = new int[obstacles.size()][2];

        // BotLogger.log("TESTING SIZE -> " + obstacles.size());
        for (int i = 0; i < obstacles.size(); i++) {

            Pair<Integer, Integer> position = obstacles.get(i);
            blocks[i] = new int[]{position.getKey(), position.getValue()};

            // BotLogger.log("OBSTACLE :: (" + position.getKey() + ", " + position.getValue() + ")");

        }

        /*for(int i = 0; i < 25; i++) {
            for (int j = 0; j < 25; j++) {
                BattleCell current = matrix[i][j];
                if (current == null) {
                    System.out.print("00  ");
                    continue;
                }
                if (current.hasEnemies(starter))
                    System.out.print("XX  ");
                else
                    System.out.print("00  ");
            }
            BotLogger.log();
        }*/

        return blocks;

    }


    public void sendPacketToViewers(List<Packet> packets) {
        if (watchMode) {
            this.packets.addAll(packets);
            return;
        }

        for (LoggedGameUser loggedGameUser : getViewers()) {
            loggedGameUser.getSmartServer().send(packets);
        }
    }

    public void sendPacketToViewers(Packet... packets) {
        if (watchMode) {
            this.packets.addAll(Arrays.asList(packets));
            return;
        }

        for (LoggedGameUser loggedGameUser : getViewers()) {
            loggedGameUser.getSmartServer().send(packets);
        }
    }

    public boolean canContinue() {

        boolean attackers = fleets.stream().filter(fleet -> !fleet.isDestroyed()).anyMatch(BattleFleet::isAttacker) || forts.stream().filter(fort -> !fort.isDestroyed()).anyMatch(BattleFort::isAttacker);
        boolean defenders = fleets.stream().filter(fleet -> !fleet.isDestroyed()).anyMatch(BattleFleet::isDefender) || forts.stream().filter(fort -> !fort.isDestroyed()).anyMatch(BattleFort::isDefender);
        return attackers && defenders;
    }

    public Set<LoggedGameUser> getViewers() {

        Set<LoggedGameUser> viewers = new HashSet<>();
        if (getMatchType().isVirtual() || getGalaxyId() == -1) {
            viewers.addAll(LoginService.getInstance().getMatchViewers(id));
        } else {
            viewers.addAll(Stream.concat(LoginService.getInstance().getMatchViewers(id).stream(), LoginService.getInstance().getPlanetViewers(getGalaxyId()).stream())
                .collect(Collectors.toList()));
        }
        return viewers;
    }

}

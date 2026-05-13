package com.go2super.service;

import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.database.entity.type.MatchType;
import com.go2super.obj.game.CorpLeaderboard;
import com.go2super.obj.game.RankFightInfo;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.game.UserLeaderboard;
import com.go2super.service.battle.GameBattle;
import com.go2super.service.battle.match.WarMatch;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

@Service
public class RankService {

    private static final Map<Integer, UserLeaderboard> cachedUser = new HashMap<>();

    private static List<List<RankFightInfo>> cachedFights = new ArrayList<>();
    private static List<List<CorpLeaderboard>> cachedCorps = new ArrayList<>();

    private static List<List<UserLeaderboard>> cachedAttackPowerRank = new ArrayList<>();
    private static List<List<UserLeaderboard>> cachedShootdownsRank = new ArrayList<>();

    private static RankService instance;

    @Getter
    private final UserCache userCache;

    public RankService(UserCache userCache) {

        instance = this;

        this.userCache = userCache;

    }

    public void setup() {

        for (User user : userCache.findAll()) {
            update(user);
        }

    }

    public void sort() {

        // Fights
        List<GameBattle> battles = BattleService.getInstance().getBattles(MatchType.PVP_MATCH);
        cachedFights = Lists.partition(battles.stream().map(battle -> {
            WarMatch warMatch = (WarMatch) battle.getMatch();
            Planet planet = warMatch.getTargetPlanet();

            RankFightInfo rankFightInfo = new RankFightInfo();
            rankFightInfo.setGalaxyId(battle.getMatch().getGalaxyId());
            rankFightInfo.setGalaxyId(battle.getMatch().getGalaxyId());

            switch (planet.getType()) {
                case USER_PLANET -> {
                    UserPlanet userPlanet = (UserPlanet) planet;
                    Optional<User> optionalUser = userPlanet.getUser();
                    if (optionalUser.isPresent()) {

                        User user = optionalUser.get();
                        rankFightInfo.setUserId(user.getUserId());
                        rankFightInfo.getName().value(user.getUsername());

                        Corp corp = user.getCorp();
                        if (corp != null) {
                            rankFightInfo.getConsortiaName().value(corp.getName());
                        }

                        rankFightInfo.setStarType(0);
                        rankFightInfo.setGuid(user.getGuid());

                    }
                }
                case RESOURCES_PLANET -> {

                    ResourcePlanet resourcePlanet = (ResourcePlanet) planet;

                    Optional<Corp> optionalCorp = resourcePlanet.getCorp();
                    if (optionalCorp.isPresent()) {
                        rankFightInfo.getConsortiaName().value(optionalCorp.get().getName());
                    }

                    rankFightInfo.setStarType(1);
                    rankFightInfo.setGuid(-1);

                }
                case HUMAROID_PLANET -> {

                    rankFightInfo.setStarType(2);
                    rankFightInfo.setGuid(-1);

                }
            }

            return rankFightInfo;

        }).collect(Collectors.toList()), 6);

        // Corps
        List<Corp> corps = CorpService.getInstance().getCorpCache().findAll();
        corps.sort((corp1, corp2) -> {
            if (corp1.getResourcePlanets().size() > corp2.getResourcePlanets().size()) {
                return -1;
            }
            if (corp1.getResourcePlanets().size() < corp2.getResourcePlanets().size()) {
                return 1;
            }
            if (corp1.getWealth() > corp2.getWealth()) {
                return -1;
            }
            if (corp1.getWealth() < corp2.getWealth()) {
                return 1;
            }
            return 0;
        });

        List<CorpLeaderboard> corpLeaderboards = corps.stream().map(corp -> CorpLeaderboard.builder()
            .corpId(corp.getCorpId())
            .rankId(corps.indexOf(corp) + 1)
            .lastCorp(corp)
            .build()).collect(Collectors.toList());

        cachedCorps = Lists.partition(corpLeaderboards, 6);

        // Users
        LinkedList<UserLeaderboard> shootdowns = new LinkedList<>(cachedUser.values());
        LinkedList<UserLeaderboard> attack = new LinkedList<>(cachedUser.values());

        Collections.sort(shootdowns, (o1, o2) -> {
            if (o1.getShootdowns() < o2.getShootdowns()) {
                return 1;
            } else if (o1.getShootdowns() > o2.getShootdowns()) {
                return -1;
            } else {
                return o1.getGuid() >= o2.getGuid() ? 1 : -1;
            }
        });

        cachedShootdownsRank = Lists.partition(shootdowns, 6);

        Collections.sort(attack, (o1, o2) -> {
            if (o1.getAttackPower() < o2.getAttackPower()) {
                return 1;
            } else if (o1.getAttackPower() > o2.getAttackPower()) {
                return -1;
            } else {
                return o1.getGuid() >= o2.getGuid() ? 1 : -1;
            }
        });

        cachedAttackPowerRank = Lists.partition(attack, 6);

    }

    public Pair<Integer, List<RankFightInfo>> getFightsByPageId(int page) {

        if (page >= 0 && cachedFights.size() > page) {
            return Pair.of(page, cachedFights.get(page));
        }

        return Pair.of(0, new ArrayList<>());

    }

    public Pair<Integer, List<UserLeaderboard>> getShootdownsByPageId(int page) {

        if (page >= 0 && cachedShootdownsRank.size() > page) {
            return Pair.of(page, cachedShootdownsRank.get(page));
        }

        return Pair.of(0, new ArrayList<>());

    }

    public Pair<Integer, List<UserLeaderboard>> getShootdownsByGuid(int guid) {

        if (!cachedUser.containsKey(guid)) {

            User user = UserService.getInstance().getUserCache().findByGuid(guid);

            if (user == null) {
                return Pair.of(0, cachedShootdownsRank.get(0));
            }

            update(user);
            sort();

        }

        for (int page = 0; page < cachedShootdownsRank.size(); page++) {
            List<UserLeaderboard> users = cachedShootdownsRank.get(page);
            for (UserLeaderboard user : users) {
                if (user.getGuid() == guid) {
                    return Pair.of(page, users);
                }
            }
        }

        return Pair.of(0, cachedShootdownsRank.get(0));

    }

    public Pair<Integer, List<UserLeaderboard>> getAttackPowerRankByPageId(int page) {

        if (page >= 0 && cachedAttackPowerRank.size() > page) {
            return Pair.of(page, cachedAttackPowerRank.get(page));
        }

        return Pair.of(0, new ArrayList<>());

    }

    public Pair<Integer, List<UserLeaderboard>> getAttackPowerRankByGuid(int guid) {

        if (!cachedUser.containsKey(guid)) {

            User user = UserService.getInstance().getUserCache().findByGuid(guid);

            if (user == null) {
                return Pair.of(0, cachedAttackPowerRank.get(0));
            }

            update(user);
            sort();

        }

        for (int page = 0; page < cachedAttackPowerRank.size(); page++) {
            List<UserLeaderboard> users = cachedAttackPowerRank.get(page);
            for (UserLeaderboard user : users) {
                if (user.getGuid() == guid) {
                    return Pair.of(page, users);
                }
            }
        }

        return Pair.of(0, cachedAttackPowerRank.get(0));

    }

    public int getAttackPowerPages() {

        return cachedAttackPowerRank.size();
    }

    public int getShootdownsPages() {

        return cachedShootdownsRank.size();
    }

    public int getFightsPages() {

        return cachedFights.size();
    }

    public synchronized void add(User user) {

        UserLeaderboard userLeaderboard = update(user);

        if (!cachedUser.containsKey(userLeaderboard.getGuid())) {
            cachedUser.put(userLeaderboard.getGuid(), userLeaderboard);
        }

        if (cachedAttackPowerRank.size() == 0 || cachedShootdownsRank.size() == 0) {
            sort();
        }

    }

    public UserLeaderboard update(User user) {

        boolean contains = cachedUser.containsKey(user.getGuid());
        UserLeaderboard leaderboard = contains ? cachedUser.get(user.getGuid()) : new UserLeaderboard();

        leaderboard.setGuid(user.getGuid());
        leaderboard.setShootdowns(user.getStats().getKills());
        leaderboard.setAttackPower(getAttackPower(user));

        if (!contains) {
            cachedUser.put(user.getGuid(), leaderboard);
        }

        return leaderboard;

    }

    public int getAttackPower(User user) {

        Long attackPower = (long) 0;

        for (ShipTeamNum shipTeamNum : PacketService.getInstance().getAllShipNums(user)) {
            if (shipTeamNum.getShipModelId() > -1 && shipTeamNum.getNum() > 0) {

                ShipModel model = PacketService.getShipModel(shipTeamNum.getShipModelId());
                if(model != null){
                    attackPower += (Long.valueOf(model.getMinAttack()) * Long.valueOf(shipTeamNum.getNum()));
                }
            }
        }

        attackPower = Long.valueOf(attackPower / 1000);
        return attackPower > Integer.MAX_VALUE ? Integer.MAX_VALUE : attackPower.intValue();

    }

    public int getAttackPowerRank(User user) {

        int rank = 0;

        for (List<UserLeaderboard> page : new CopyOnWriteArrayList<>(cachedAttackPowerRank)) {
            for (UserLeaderboard entry : page) {

                if (entry.getGuid() == user.getGuid()) {
                    return rank;
                }

                rank++;

            }
        }

        return rank;

    }

    public int getShootdownsRank(User user) {

        int rank = 0;

        for (List<UserLeaderboard> page : new CopyOnWriteArrayList<>(cachedShootdownsRank)) {
            for (UserLeaderboard entry : page) {

                if (entry.getGuid() == user.getGuid()) {
                    return rank;
                }

                rank++;

            }
        }

        return rank;

    }

    public static void deleteUser(UserLeaderboard userLeaderboard) {

        cachedUser.remove(userLeaderboard);

    }

    public static List<UserLeaderboard> getCache() {

        return new ArrayList<>(cachedUser.values());
    }

    public static List<CorpLeaderboard> getCorpCache() {

        return cachedCorps.stream()
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    public static int getCorpRank(int corpId) {

        int rank = 0;
        for (List<CorpLeaderboard> corpLeaderboards : new CopyOnWriteArrayList<>(cachedCorps)) {
            for (CorpLeaderboard entry : corpLeaderboards) {
                if (entry.getCorpId() == corpId) {
                    return rank;
                }
            }
        }
        return rank;
    }

    public static RankService getInstance() {

        return instance;
    }

}

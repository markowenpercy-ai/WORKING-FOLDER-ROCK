package com.go2super.service;


import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserIglStats;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.game.RacingReportInfo;
import com.go2super.packet.igl.RacingRank;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

@Data
@Getter
@Service
public class IGLService {


    @Getter
    public static IGLService instance;

    private ConcurrentHashMap<Long, List<Integer>> userToFleetIds = new ConcurrentHashMap<>();
    private ConcurrentHashMap<Long, LinkedList<RacingReportInfo>> userToReport = new ConcurrentHashMap<>();

    private List<RacingRank> rankList = new CopyOnWriteArrayList<>();

    private AtomicBoolean enabled = new AtomicBoolean(true);

    @Getter
    private final UserCache userCache;

    @Autowired
    public IGLService(UserCache userCache) {
        instance = this;
        this.userCache = userCache;
        setup();
    }

    private void setup() {
        rankList.clear();
        userToFleetIds.clear();

        List<User> initialUsers = new ArrayList<>(userCache.findAll().stream().filter(user -> user.getBuildings().getBuilding("build:galaxyTransporter") != null).toList());
        initialUsers.sort(Comparator.comparingInt(o -> o.getStats().getUserIglStats().getRank()));
        for (int i = 0; i < initialUsers.size(); i++) {
            User user = initialUsers.get(i);
            UserIglStats userIglStats = user.getStats().getUserIglStats();
            boolean toUpdate = false;
            if (userIglStats.getRank() != i || userIglStats.getEntries() > 0) {
                userIglStats.setRank(i);
                userIglStats.setEntries(0);
                toUpdate = true;
            }
            userIglStats.setRank(i);
            rankList.add(RacingRank.builder().name(user.getUsername()).userId(user.getUserId()).rankId(i).build());
            if (toUpdate) {
                userCache.save(user);
            }
        }
        rankList.sort(Comparator.comparingInt(RacingRank::getRankId));
        System.out.println("IGLService setup complete");
    }


    public void addFleetIds(long userId, IntegerArray fleetIds) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < fleetIds.getArray().length; i++) {
            if (fleetIds.getArray()[i] != 0) {
                list.add(fleetIds.getArray()[i]);
            }
        }
        userToFleetIds.put(userId, list);
    }

    public List<Integer> getFleetIds(long userId) {
        List<Integer> fleetIds = userToFleetIds.getOrDefault(userId, new ArrayList<>());
        if (fleetIds.isEmpty()) {
            return fleetIds;
        }

        for (Iterator<Integer> iterator = fleetIds.iterator(); iterator.hasNext(); ) {
            Integer fleetId = iterator.next();
            Fleet byShipTeamId = PacketService.getInstance().getFleetCache().findByShipTeamId(fleetId);
            if (byShipTeamId == null) {
                iterator.remove();
            }
        }
        return fleetIds;
    }

    private Pair<Integer, List<RacingRank>> getPaged(int page, int perPage) {
        int start = page * perPage;
        int end = start + perPage;
        if (end > rankList.size()) {
            end = rankList.size();
        }
        return Pair.of(page, rankList.subList(start, end));
    }

    public Pair<Integer, List<RacingRank>> getIglRankPage(int page) {
        return getPaged(page, 6);
    }

    public Pair<Integer, List<RacingRank>> getIglRankPageByGuid(int guid) {
        Optional<RacingRank> first = rankList.stream().filter(racingRank -> racingRank.getUserId() == guid).findFirst();
        if (first.isEmpty()) {
            return getIglRankPage(0);
        }
        int rankId = first.get().rankId;
        int page = rankId / 6;
        return getIglRankPage(page);
    }

    public int totalCount() {
        return rankList.size();
    }


    public RacingRank findByGuid(User user) {
        Optional<RacingRank> any = rankList.stream().filter(racingRank -> racingRank.getUserId() == user.getUserId()).findAny();
        if (any.isPresent()) {
            return any.get();
        }
        RacingRank build = RacingRank.builder().name(user.getUsername()).userId(user.getUserId()).rankId(rankList.size()).build();
        rankList.add(build);
        return build;
    }


    public List<RacingRank> getIglPageByGuid(int userId) {
        List<RacingRank> resp = new ArrayList<>();
        Optional<RacingRank> first = rankList.stream().filter(racingRank -> racingRank.getUserId() == userId).findFirst();
        first.ifPresent(x -> {
            if (x.getRankId() < 10) {
                resp.addAll(rankList.subList(0, Math.min(10, rankList.size())));
            } else {
                resp.addAll(rankList.subList(x.getRankId()- 9, Math.min(x.getRankId(), rankList.size())));
            }
            resp.add(x);
        });
        return resp;
    }

    public synchronized void win(int userId1, int userId2) {
        Optional<RacingRank> first = rankList.stream().filter(racingRank -> racingRank.getUserId() == userId1).findFirst();
        Optional<RacingRank> sec = rankList.stream().filter(racingRank -> racingRank.getUserId() == userId2).findFirst();

        if (first.isPresent() && sec.isPresent()) {
            RacingRank firstRank = first.get();
            RacingRank secRank = sec.get();
            int firstRankId = firstRank.getRankId();
            int secRankId = secRank.getRankId();

            if (firstRankId >= secRankId) {
                firstRank.setRankId(secRankId);
                secRank.setRankId(firstRankId);
                User user = userCache.findByGuid(userId1);
                if (user != null) {
                    user.getStats().getIglStats().setRank(firstRank.getRankId());
                    user.save();
                    userCache.save(user);
                }
                User user1 = userCache.findByGuid(userId2);
                if (user1 != null) {
                    user1.getStats().getIglStats().setRank(secRank.getRankId());
                    user1.save();
                    userCache.save(user1);
                }

                rankList.sort(Comparator.comparingInt(RacingRank::getRankId));
            }

            RacingReportInfo userOneInfo = RacingReportInfo.builder()
                    .type(1)
                    .time(1)
                    .reportDate(1)
                    .rankChange(10000)
                    .username(secRank.getName())
                    .build();
            addRacingReportInfo(userId1, userOneInfo);

            RacingReportInfo userTwoInfo = RacingReportInfo.builder()
                    .type(0)
                    .time(1)
                    .reportDate(1)
                    .rankChange(-10000)
                    .username(firstRank.getName())
                    .build();
            addRacingReportInfo(userId2, userTwoInfo);


        }
    }

    public synchronized void lossOrDraw(int userId1, int userId2) {
        Optional<RacingRank> first = rankList.stream().filter(racingRank -> racingRank.getUserId() == userId1).findFirst();
        Optional<RacingRank> sec = rankList.stream().filter(racingRank -> racingRank.getUserId() == userId2).findFirst();

        if (first.isPresent() && sec.isPresent()) {
            RacingRank firstRank = first.get();
            RacingRank secRank = sec.get();

            RacingReportInfo userOneInfo = RacingReportInfo.builder()
                    .type(1)
                    .time(1)
                    .reportDate(1)
                    .rankChange(0)
                    .username(secRank.getName())
                    .build();
            addRacingReportInfo(userId1, userOneInfo);

            RacingReportInfo userTwoInfo = RacingReportInfo.builder()
                    .type(0)
                    .time(1)
                    .reportDate(1)
                    .rankChange(0)
                    .username(firstRank.getName())
                    .build();
            addRacingReportInfo(userId2, userTwoInfo);
        }
    }

    private void addRacingReportInfo(int userId, RacingReportInfo racingReportInfo) {
        userToReport.computeIfAbsent((long) userId, k -> new LinkedList<>()).add(racingReportInfo);
        while (userToReport.get((long) userId).size() > 5) {
            userToReport.get((long) userId).removeFirst();
        }
    }

    public List<RacingReportInfo> getRacingReportInfo(long userId) {
        return userToReport.getOrDefault(userId, new LinkedList<>());
    }


    public void registerUser(User user) {
        UserIglStats userIglStats = user.getStats().getUserIglStats();
        userIglStats.setRank(rankList.size());
        userIglStats.setEntries(0);
        userIglStats.setClaimed(false);
        rankList.add(RacingRank.builder().name(user.getUsername()).userId(user.getUserId()).rankId(rankList.size()).build());
        user.save();
        userCache.save(user);
    }
}

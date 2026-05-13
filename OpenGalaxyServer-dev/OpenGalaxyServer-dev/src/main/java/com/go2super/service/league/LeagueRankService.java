package com.go2super.service.league;

import com.go2super.database.cache.UserCache;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserLeagueStats;
import com.go2super.logger.BotLogger;
import com.go2super.obj.game.UserLeagueLeaderboard;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.LeagueData;
import com.go2super.resources.json.LeaguesJson;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;

@Getter
@Service
public class LeagueRankService {

    @Getter
    private static LeagueRankService instance;

    private final static int MAX_RANK = 10;

    private static final List<List<UserLeagueLeaderboard>> cachedLeagueRank = new ArrayList<>(MAX_RANK);

    private static final Map<Integer, UserLeagueLeaderboard> cachedUser = new HashMap<>();

    private final LeaguesJson leaguesJson;

    @Getter
    private final UserCache userCache;

    @Autowired
    public LeagueRankService(UserCache userCache) {
        instance = this;
        this.leaguesJson = ResourceManager.getLeaguesJson();
        this.userCache = userCache;
        setup();
    }

    public void setup() {
        cachedUser.clear();
        cachedLeagueRank.clear();
        for (int i = 0; i < MAX_RANK; i++) {
            cachedLeagueRank.add(new ArrayList<>());
        }
        for (User user : userCache.findAll()) {
            update(user);
        }
        for (int i = 0; i < MAX_RANK; i++) {
            sortLeagueRank(i);
        }
    }

    public UserLeagueLeaderboard update(User user) {
        boolean contains = cachedUser.containsKey(user.getGuid());
        UserLeagueLeaderboard leaderboard = contains ? cachedUser.get(user.getGuid()) : new UserLeagueLeaderboard();

        leaderboard.setGuid(user.getGuid());
        UserLeagueStats stats = user.getStats().getLeagueStats();
        if (stats != null) {
            leaderboard.setWins(stats.getWins());
            leaderboard.setLosses(stats.getLosses());
            leaderboard.setDraws(stats.getDraws());
            leaderboard.setLeague(stats.getLeague());
        } else {
            stats = UserLeagueStats.builder().wins(0)
                    .losses(0)
                    .draws(0)
                    .league(0)
                    .build();
            leaderboard.setWins(0);
            leaderboard.setLosses(0);
            leaderboard.setDraws(0);
            leaderboard.setLeague(0);
            user.getStats().setLeagueStats(stats);
            userCache.save(user);
        }

        cachedUser.put(user.getGuid(), leaderboard);
        if (!contains) {
            cachedLeagueRank.get(stats.getLeague()).add(leaderboard);
        }
        return leaderboard;
    }

    private void sortLeagueRank(int league) {
        List<UserLeagueLeaderboard> rank = cachedLeagueRank.get(league);
        rank.sort(Comparator.comparingInt(UserLeagueLeaderboard::getPoints));
        Collections.reverse(rank);
        for (int i = 0; i < rank.size(); i++) {
            rank.get(i).setRank(i + 1);
        }
    }

    public void addNewUser(User user) {
        update(user);
        sortLeagueRank(user.getStats().getLeagueStats().getLeague());
    }

    public Pair<Integer, List<UserLeagueLeaderboard>> getPaged(int page) {
        List<UserLeagueLeaderboard> xs = Lists.reverse(cachedLeagueRank).stream().flatMap(List::stream).toList();

        int startIndex = page * 10;
        int endIndex = Math.min(xs.size(), (page + 1) * 10);

        if (startIndex >= xs.size()) {
            return Pair.of(page, Collections.emptyList());
        }

        var sublist = xs.subList(startIndex, endIndex);
        return Pair.of(page, sublist);
    }


    public int totalCount() {
        return cachedLeagueRank.stream().flatMap(List::stream).toList().size();
    }

    public Pair<Integer, List<UserLeagueLeaderboard>> getPagedByGuid(int userId) {
        int pageidx = 0;
        Pair<Integer, List<UserLeagueLeaderboard>> page = getPaged(pageidx++);
        while (page != null && !page.getRight().isEmpty()) {
            for (UserLeagueLeaderboard rank : page.getRight()) {
                if (rank.getGuid() == userId) {
                    return page;
                }
            }
            page = getPaged(pageidx++);
        }
        return null;
    }

    public UserLeagueLeaderboard getByGuid(int userId) {
        if (!cachedUser.containsKey(userId)) {
            addNewUser(userCache.findByGuid(userId));
        }
        return cachedUser.get(userId);
    }


    public void addWin(User user) {
        UserLeagueStats stats = user.getStats().getLeagueStats();
        stats.setWins(stats.getWins() + 1);
        update(user);
        sortLeagueRank(stats.getLeague());
    }

    public void addLoss(User user) {
        UserLeagueStats stats = user.getStats().getLeagueStats();
        stats.setLosses(stats.getLosses() + 1);
        update(user);
        sortLeagueRank(stats.getLeague());
    }

    public void addDraw(User user) {
        UserLeagueStats stats = user.getStats().getLeagueStats();
        stats.setDraws(stats.getDraws() + 1);
        update(user);
        sortLeagueRank(stats.getLeague());
    }


    @Scheduled(cron = "0 0 0 * * MON")
    public void weeklyUpdate() {
        BotLogger.info("Weekly league update");
        List<LeagueData> leagueDataList = getLeaguesJson().getLeagues();

        for (User user: userCache.findAll()) {
            LeagueData l = leagueDataList.get(user.getCurrentLeague());
            List<UserLeagueLeaderboard> rank = cachedLeagueRank.get(user.getCurrentLeague());

            UserLeagueStats stats = user.getStats().getLeagueStats();
            stats.setWins(0);
            stats.setLosses(0);
            stats.setDraws(0);

            Integer unchanged = l.getUnchanged();
            if (unchanged == null) {
                unchanged = Math.max(0, rank.size() - l.getPromoted());
            }

            int promotedStart = 0;
            int promotedEnd = Math.min(rank.size(), l.getPromoted());

            int unchangedEnd = Math.min(rank.size(), l.getPromoted() + unchanged);
            int demotedEnd = Math.min(rank.size(), l.getPromoted() + unchanged + l.getDowngraded());

            int myRank = rank.indexOf(getByGuid(user.getGuid()));

            if (myRank >= promotedStart && myRank < promotedEnd) {
                stats.setLeague(Math.min(user.getCurrentLeague() + 1, MAX_RANK - 1));
                BotLogger.info("User " + user.getGuid() + " promoted to league " + user.getCurrentLeague());
            } else if (myRank >= promotedEnd && myRank < unchangedEnd) {
                BotLogger.info("User " + user.getGuid() + " unchanged in league " + user.getCurrentLeague());
                stats.setLeague(user.getCurrentLeague());
            } else if (myRank >= unchangedEnd && myRank < demotedEnd) {
                stats.setLeague(Math.max(user.getCurrentLeague() - 1, 0));
                BotLogger.info("User " + user.getGuid() + " demoted to league " + user.getCurrentLeague());
            }
            userCache.save(user);
        }

        setup();
    }

}

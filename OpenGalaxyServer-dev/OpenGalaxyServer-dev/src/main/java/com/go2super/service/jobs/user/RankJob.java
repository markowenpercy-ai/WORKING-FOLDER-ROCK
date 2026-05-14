package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.obj.game.UserLeaderboard;
import com.go2super.service.RankService;
import com.go2super.service.UserService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;
import lombok.SneakyThrows;

import java.util.*;
import java.util.stream.*;

public class RankJob implements OfflineJob {

    public List<User> toRankAdd = new ArrayList<>();

    private long lastExecution = 0L;
    private boolean setup = false;

    @Override
    @SneakyThrows
    public void setup() {

        RankService.getInstance().setup();
        setup = true;

    }

    @Override
    @SneakyThrows
    public void run() {

        if (!setup) {
            setup();
        }

        if (DateUtil.millis() - lastExecution < getInterval()) {
            return;
        }
        lastExecution = DateUtil.millis();

        List<Integer> userIds = RankService.getCache().stream().map(UserLeaderboard::getGuid).collect(Collectors.toList());
        List<User> users = UserService.getInstance().getUserCache().findByGuid(userIds);

        for (UserLeaderboard userLeaderboard : RankService.getCache()) {

            Optional<User> optionalUser = users.stream().filter(user -> user.getGuid() == userLeaderboard.getGuid()).findFirst();

            if (optionalUser.isEmpty()) {

                RankService.deleteUser(userLeaderboard);
                continue;

            }

            RankService.getInstance().update(optionalUser.get());
            Thread.sleep(10);

        }

        for (User user : toRankAdd) {
            RankService.getInstance().add(user);
        }

        toRankAdd.clear();
        RankService.getInstance().sort();

    }

    public void synchronizedAdd(User user) {

        toRankAdd.add(user);
    }

    @Override
    public long getInterval() {

        return 2000L;
    }

}

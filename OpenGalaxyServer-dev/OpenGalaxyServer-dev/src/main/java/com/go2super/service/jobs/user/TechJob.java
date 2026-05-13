package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserTech;
import com.go2super.packet.science.ResponseCreateTechCompletePacket;
import com.go2super.service.ResourcesService;
import com.go2super.service.UserService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class TechJob implements OfflineJob {

    private long lastExecution = 0L;

    @Override
    public void setup() {

    }

    @Override
    public void run() {

        if (DateUtil.millis() - lastExecution < getInterval()) {
            return;
        }
        lastExecution = DateUtil.millis();

        List<User> users = UserService.getInstance().getUserCache().findByTechUpgrading();
        CopyOnWriteArrayList<Integer> toUpdate = new CopyOnWriteArrayList<>();

        for (User user : users) {

            if (user == null) {
                continue;
            }
            if (user.getTechs().getUpgrade() == null) {
                continue;
            }
            if (user.getTechs().getUpgrade().getUntil() == null) {
                continue;
            }

            if (DateUtil.remains(user.getTechs().getUpgrade().getUntil()).intValue() <= 0) {
                toUpdate.add(user.getGuid());
            }

        }

        if (!toUpdate.isEmpty()) {
            List<User> toUpdateUsers = UserService.getInstance().getUserCache().findByGuid(toUpdate);

            for (User user : toUpdateUsers) {

                if (user == null) {
                    continue;
                }
                if (user.getTechs().getUpgrade() == null) {
                    continue;
                }
                if (user.getTechs().getUpgrade().getUntil() == null) {
                    continue;
                }

                if (DateUtil.remains(user.getTechs().getUpgrade().getUntil()).intValue() <= 0) {

                    UserTech techUser = user.getTechs().getTech(user.getTechs().getUpgrade().getId());

                    if (techUser != null) {

                        techUser.setLevel(techUser.getLevel() + 1);

                    } else {

                        user.getTechs().getTechs().add(UserTech.builder()
                                .id(user.getTechs().getUpgrade().getId())
                                .level(user.getTechs().getUpgrade().getLevel())
                                .build());

                    }

                    ResponseCreateTechCompletePacket response = ResponseCreateTechCompletePacket.builder()
                            .techId(user.getTechs().getUpgrade().getId())
                            .build();

                    user.getTechs().setUpgrade(null);

                    user.update();
                    user.save();

                    if (user.isOnline()) {

                        user.getLoggedGameUser().get().getSmartServer().send(response);
                        user.getLoggedGameUser().get().getSmartServer().send(ResourcesService.getInstance().getPlayerResourcePacket(user));

                    }

                }

            }

        }

    }

    @Override
    public long getInterval() {

        return 500L;
    }

}



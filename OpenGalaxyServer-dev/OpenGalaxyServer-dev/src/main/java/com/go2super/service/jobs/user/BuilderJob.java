package com.go2super.service.jobs.user;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuilding;
import com.go2super.database.entity.sub.UserBuildings;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.packet.construction.ResponseBuildCompletePacket;
import com.go2super.packet.props.ResponseUsePropsPacket;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.service.ResourcesService;
import com.go2super.service.jobs.GalaxyUserJob;

import java.util.*;

public class BuilderJob implements GalaxyUserJob {

    @Override
    public String getName() {

        return "builder-job";
    }

    @Override
    public boolean needUpdate(User user) {

        UserBuildings buildings = user.getBuildings();
        if (buildings.getBuildings().isEmpty()) {
            return false;
        }

        return buildings.getBuildings().stream()
            .anyMatch(building -> building.getUpdating() != null && building.getUpdating() && building.updatingTime() <= 0);

    }

    @Override
    public boolean run(LoggedGameUser loggedGameUser, User user) {

        UserBuildings buildings = user.getBuildings();
        if (buildings.getBuildings().isEmpty()) {
            return false;
        }

        boolean update = false;

        for (UserBuilding building : buildings.getBuildings()) {
            if (building.getUpdating() != null && building.getUpdating()) {
                if (building.updatingTime() <= 0) {

                    update = true;
                    building.setLevelId(building.getLevelId() + 1);
                    building.setUpdating(false);
                    building.setUntilUpdate(null);

                    BuildLevelMeta buildLevelMeta = building.getData().getLevel(building.getLevelId());

                    user.getStats().addExp((int) (buildLevelMeta.getMetal() + buildLevelMeta.getGas() + buildLevelMeta.getGold()));

                    ResponseBuildCompletePacket response = buildCompletePacket(0, user.getPlanet().getPosition().galaxyId(), buildings.getBuildings().indexOf(building));
                    loggedGameUser.getSmartServer().send(response);

                    if (building.getLevelId() == 0 && building.getData().getType().equals("space")) {
                        user.getMetrics().add("action:build.space.structure", 1);
                    }

                    if (building.getLevelId() == 0 && building.getData().getName().equals("build:commander")) {

                        user.getResources().setVouchers(user.getResources().getVouchers() + 1000);

                        Optional<LoggedGameUser> optional = user.getLoggedGameUser();

                        if (optional.isPresent()) {

                            LoggedGameUser gameUser = optional.get();

                            ResponseUsePropsPacket usePropsPacket = ResourcesService.getInstance().genericUseProps(-1, 0, 0, 0, 0, 1000, 0, 0, 0, 0, 1, 0);

                            gameUser.getSmartServer().send(usePropsPacket);
                            gameUser.getSmartServer().sendMessage("You have received 1000 vouchers!");

                        }

                    }

                }

            } else if (building.getRepairing() != null && building.getRepairing()) {

                if (building.repairingTime() <= 0) {

                    update = true;
                    building.setRepairing(false);
                    building.setUntilRepair(null);

                    ResponseBuildCompletePacket response = buildCompletePacket(0, user.getPlanet().getPosition().galaxyId(), buildings.getBuildings().indexOf(building));
                    loggedGameUser.getSmartServer().send(response);

                }

            }
        }

        if (update) {
            loggedGameUser.getSmartServer().send(ResourcesService.getInstance().getPlayerResourcePacket(user));
        }
        return update;

    }

    public ResponseBuildCompletePacket buildCompletePacket(int gmap, int gid, int indexId) {

        ResponseBuildCompletePacket buildCompletePacket = new ResponseBuildCompletePacket();

        buildCompletePacket.setGalaxyMapId(gmap);
        buildCompletePacket.setGalaxyId(gid);
        buildCompletePacket.setIndexId(indexId);

        return buildCompletePacket;

    }

}

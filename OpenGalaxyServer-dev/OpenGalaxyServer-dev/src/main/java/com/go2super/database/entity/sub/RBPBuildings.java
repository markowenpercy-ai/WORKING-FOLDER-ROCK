package com.go2super.database.entity.sub;

import com.go2super.logger.BotLogger;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class RBPBuildings {

    private LinkedList<RBPBuilding> buildings;

    public RBPBuilding pickOne(String name) {

        for (RBPBuilding userBuilding : buildings) {
            if (ResourceManager.getBuilds().getBuild(userBuilding.getBuildingId()).getName().equals(name)) {
                return userBuilding;
            }
        }
        return null;
    }

    public int count(int building) {

        int result = 0;
        for (RBPBuilding cache : buildings) {
            if (cache.getBuildingId() == building) {
                result++;
            }
        }
        return result;
    }

    public RBPBuilding getBuilding(int index) {

        if (buildings.size() > index && index > -1) {
            return buildings.get(index);
        }
        return null;
    }

    public List<RBPBuilding> getBuildings(String name) {

        BuildData buildData = ResourceManager.getBuilds().getBuild(name);
        List<RBPBuilding> filter = new ArrayList<>();

        if (buildData == null) {
            BotLogger.error("Not found building with name: " + name);
            return filter;
        }

        for (RBPBuilding building : buildings) {
            if (building.getBuildingId() == buildData.getId()) {
                filter.add(building);
            }
        }

        return filter;

    }

    public RBPBuilding getBuilding(String name) {

        List<RBPBuilding> buildings = getBuildings(name);

        if (buildings.isEmpty()) {
            return null;
        }

        return buildings.get(0);

    }

    public List<RBPBuilding> getBuildings(int id) {

        List<RBPBuilding> filter = new ArrayList<>();
        for (RBPBuilding building : buildings) {
            if (building.getBuildingId() == id) {
                filter.add(building);
            }
        }
        return filter;
    }

    public List<RBPBuilding> getUpdatingBuildings() {

        List<RBPBuilding> filter = new ArrayList<>();
        for (RBPBuilding building : buildings) {
            if (building.getUpdating() != null && building.getUpdating()) {
                filter.add(building);
            }
        }
        return filter;
    }

    public boolean has(int id, int level) {

        for (RBPBuilding building : getBuildings(id)) {
            if (building.getLevelId() == level) {
                return true;
            }
        }
        return false;
    }

    public RBPBuildings addBuilding(int building, int level, int x, int y) {

        return addBuilding(RBPBuilding.builder().buildingId(building).levelId(level).x(x).y(y).index(nextIndexId()).build());
    }

    public RBPBuildings addBuilding(RBPBuilding building) {

        if (buildings == null) {
            buildings = new LinkedList<>();
        }

        buildings.add(building);
        return this;

    }

    public boolean refresh() {

        boolean needRefresh = false;

        for (RBPBuilding building : buildings) {
            if (building.getIndex() == -1) {
                needRefresh = true;
                break;
            }
        }

        if (!needRefresh) {
            return false;
        }
        int index = 0;

        for (RBPBuilding building : buildings) {
            building.setIndex(index++);
        }

        return true;

    }

    public int nextIndexId() {

        int index = 0;
        if (buildings == null) {
            return 0;
        }

        for (RBPBuilding building : buildings) {
            if (building.getIndex() > index) {
                index = building.getIndex();
            }
        }

        return index + 1;

    }

}

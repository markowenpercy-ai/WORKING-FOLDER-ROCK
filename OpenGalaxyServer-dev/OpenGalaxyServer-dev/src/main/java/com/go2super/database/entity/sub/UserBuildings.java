package com.go2super.database.entity.sub;

import com.go2super.logger.BotLogger;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.meta.BuildLevelMeta;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserBuildings {

    private LinkedList<UserBuilding> buildings;

    public UserBuilding pickOne(String name) {

        for (UserBuilding userBuilding : buildings) {
            if (ResourceManager.getBuilds().getBuild(userBuilding.getBuildingId()).getName().equals(name)) {
                return userBuilding;
            }
        }
        return null;
    }

    public int count(int building) {

        int result = 0;
        for (UserBuilding cache : buildings) {
            if (cache.getBuildingId() == building) {
                result++;
            }
        }
        return result;
    }

    public int getHe3Gain() {

        return (int) getBuildingEffectSum("gainFuel");
    }

    public int getMetalGain() {

        return (int) getBuildingEffectSum("gainMetal");
    }

    public int getGoldGain() {

        return (int) getBuildingEffectSum("gainGold");
    }

    public double getGoldBonus() {

        return getBuildingEffectSum("goldBonus");
    }

    public double getMetalBonus() {

        return getBuildingEffectSum("metalBonus");
    }

    public double getHe3Bonus() {

        return getBuildingEffectSum("fuelBonus");
    }

    private double getBuildingEffectSum(String effect) {

        double gain = 0;
        List<UserBuilding> buildings = getBuildingsByEffects(effect);

        for (UserBuilding building : buildings) {

            BuildLevelMeta meta = building.getLevelData();
            gain += meta.getEffect(effect).getValue();

        }

        return gain;

    }

    public List<UserBuilding> getBuildingsByEffects(String... effects) {

        List<UserBuilding> buildings = new ArrayList<>();

        delta:
        for (UserBuilding building : getBuildings()) {

            BuildData data = building.getData();

            if (building.getLevelId() >= 0) {

                BuildLevelMeta level = data.getLevel(building.getLevelId());
                if (level == null) {
                    continue;
                }

                for (String effect : effects) {
                    if (level.getEffectNames().contains(effect)) {
                        buildings.add(building);
                        continue delta;
                    }
                }

            }
        }

        return buildings;

    }

    public UserBuilding getBuilding(int index) {

        if (buildings.size() > index && index > -1) {
            return buildings.get(index);
        }
        return null;
    }

    public List<UserBuilding> getBuildings(String name) {

        BuildData buildData = ResourceManager.getBuilds().getBuild(name);
        List<UserBuilding> filter = new ArrayList<>();

        if (buildData == null) {
            BotLogger.error("Not found building with name: " + name);
            return filter;
        }

        for (UserBuilding building : buildings) {
            if (building.getBuildingId() == buildData.getId()) {
                filter.add(building);
            }
        }

        return filter;

    }

    public UserBuilding getBuilding(String name) {

        List<UserBuilding> buildings = getBuildings(name);

        if (buildings.isEmpty()) {
            return null;
        }

        return buildings.get(0);

    }

    public List<UserBuilding> getBuildings(int id) {

        List<UserBuilding> filter = new ArrayList<>();
        for (UserBuilding building : buildings) {
            if (building.getBuildingId() == id) {
                filter.add(building);
            }
        }
        return filter;
    }

    public List<UserBuilding> getUpdatingBuildings() {

        List<UserBuilding> filter = new ArrayList<>();
        for (UserBuilding building : buildings) {
            if (building.getUpdating() != null && building.getUpdating()) {
                filter.add(building);
            }
        }
        return filter;
    }

    public boolean has(String buildingName, int level) {

        for (UserBuilding building : getBuildings(buildingName)) {
            if (building.getLevelId() >= level && !Boolean.TRUE.equals(building.getUpdating())) {
                return true;
            }
        }
        return false;
    }

    public UserBuildings addBuilding(int building, int level, int x, int y) {

        return addBuilding(UserBuilding.builder().buildingId(building).levelId(level).x(x).y(y).index(nextIndexId()).build());
    }

    public UserBuildings addBuilding(UserBuilding building) {

        if (buildings == null) {
            buildings = new LinkedList<>();
        }

        buildings.add(building);
        return this;

    }

    public boolean refresh() {

        boolean needRefresh = false;

        for (UserBuilding building : buildings) {
            if (building.getIndex() == -1) {
                needRefresh = true;
                break;
            }
        }

        if (!needRefresh) {
            return false;
        }
        int index = 0;

        for (UserBuilding building : buildings) {
            building.setIndex(index++);
        }

        return true;

    }

    public int nextIndexId() {

        int index = 0;
        if (buildings == null) {
            return 0;
        }

        for (UserBuilding building : buildings) {
            if (building.getIndex() > index) {
                index = building.getIndex();
            }
        }

        return index + 1;

    }

}

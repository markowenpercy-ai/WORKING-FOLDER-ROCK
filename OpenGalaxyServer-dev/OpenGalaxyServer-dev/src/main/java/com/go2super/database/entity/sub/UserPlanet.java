package com.go2super.database.entity.sub;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.FarmLandData;
import com.go2super.resources.json.FarmLandsJson;
import com.go2super.service.UserService;
import lombok.Data;

import java.util.*;

@Data
public class UserPlanet extends Planet {

    private String userObjectId;

    private int starFace;
    private Date untilFlag;

    public UserPlanet() {

    }

    public UserPlanet(String userObjectId, long userId, GalaxyTile galaxyTile, int starFace) {

        this.userObjectId = userObjectId;
        this.starFace = starFace;
        this.untilFlag = new Date();

        this.setUserId(userId);
        this.setType(PlanetType.USER_PLANET);
        this.setPosition(galaxyTile);

    }

    public FarmLandData getAdjacentByGalaxyId(int galaxyId) {

        List<FarmLandData> farmLands = getAdjacentFarmLands();

        for (int i = 0; i < 12; i++) {

            if (farmLands.size() < i) {
                break;
            }

            for (FarmLandData farmLand : farmLands) {
                if (farmLand.getId() == i) {
                    GalaxyTile farmLandTile = farmLand.calculateTile(this);
                    if (farmLandTile.galaxyId() == galaxyId) {
                        return farmLand;
                    }
                }
            }

        }

        return null;

    }

    public List<FarmLandData> getAdjacentFarmLands() {

        List<FarmLandData> result = new ArrayList<>();
        Optional<User> optionalUser = getUser();
        if (!optionalUser.isPresent()) {
            return result;
        }

        FarmLandsJson farmLandsJson = ResourceManager.getFarmLands();
        int spaceStationLevel = optionalUser.get().getSpaceStationLevel();

        for (FarmLandData farmLandData : farmLandsJson.getFarmLands()) {
            if (spaceStationLevel >= farmLandData.getStation()) {
                result.add(farmLandData);
            }
        }

        return result;

    }

    public Optional<User> getUser() {

        Optional<User> optionalUser = UserService.getInstance().getUserCache().findById(userObjectId);
        return optionalUser;

    }

}

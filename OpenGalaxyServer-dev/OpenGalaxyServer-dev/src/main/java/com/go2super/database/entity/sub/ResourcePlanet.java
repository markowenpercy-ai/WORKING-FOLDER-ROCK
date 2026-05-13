package com.go2super.database.entity.sub;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.database.entity.type.SpaceFortType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.resources.data.meta.FortificationEffectMeta;
import com.go2super.resources.data.meta.FortificationLevelMeta;
import com.go2super.service.CorpService;
import com.go2super.service.GalaxyService;
import com.go2super.service.UserService;
import lombok.Data;

import java.util.*;

@Data
public class ResourcePlanet extends Planet {

    private int currentCorp;
    private int currentLevel;

    private boolean fight;

    private boolean peace;
    private Date statusTime;

    private RBPBuildings rbpBuildings = RBPBuildings.builder()
        .buildings(new LinkedList<>())
        .build();

    public ResourcePlanet() {

    }

    public ResourcePlanet(GalaxyTile galaxyTile, long userId) {

        this.currentCorp = -1;
        this.currentLevel = 0;
        this.fight = false;
        this.statusTime = new Date();

        this.setUserId(userId);
        this.setType(PlanetType.RESOURCES_PLANET);
        this.setPosition(galaxyTile);

    }

    public int getMaxFleets() {

        RBPBuilding spaceStation = getSpaceStation();

        FortificationLevelMeta levelMeta = spaceStation.getLevelData();
        if (levelMeta == null) {
            return 0;
        }

        Optional<FortificationEffectMeta> optionalEffectMeta = levelMeta.getEffect("shipNum");
        if (optionalEffectMeta.isEmpty()) {
            return 0;
        }

        return (int) optionalEffectMeta.get().getValue();

    }

    public int getSSLevel() {

        RBPBuilding spaceStation = getSpaceStation();
        return spaceStation.getLevelId();
    }

    public RBPBuilding getSpaceStation() {

        return getBuilding(SpaceFortType.RBP_SPACE_STATION.getDataId()).get();
    }

    public Optional<RBPBuilding> getBuilding(int buildingId) {

        return getBuildings().getBuildings().stream().filter(rbpBuilding -> rbpBuilding.getBuildingId() == buildingId).findFirst();
    }

    public RBPBuildings getBuildings() {

        if (rbpBuildings == null) {
            rbpBuildings = RBPBuildings.builder().buildings(new LinkedList<>()).build();
        }

        if (rbpBuildings.getBuildings().isEmpty()) {
            rbpBuildings.setBuildings(GalaxyService.getInstance().createRBPBuildings());
        }

        return rbpBuildings;

    }

    public int getViewFlag(int requester) {

        User user = UserService.getInstance().getUserCache().findByGuid(requester);
        if (user == null) {
            return 0;
        }

        Corp corp = user.getCorp();
        Optional<Corp> hereCorp = getCorp();

        if (corp == null || hereCorp.isEmpty()) {
            return 0;
        }
        return corp.getCorpId() == hereCorp.get().getCorpId() ? 1 : 0;

    }

    public Optional<Corp> getCorp() {

        return Optional.ofNullable(CorpService.getInstance().getCorpCache().findByCorpId(currentCorp));
    }

    public boolean hasTruce() {

        return peace && statusTime.getTime() > new Date().getTime();
    }

}

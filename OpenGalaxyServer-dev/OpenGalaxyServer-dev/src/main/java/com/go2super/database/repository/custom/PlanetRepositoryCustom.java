package com.go2super.database.repository.custom;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.HumaroidPlanet;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.utility.GalaxyRegion;
import com.go2super.obj.utility.GalaxyTile;

import java.util.*;

public interface PlanetRepositoryCustom {

    UserPlanet getUserPlanet(User user);

    UserPlanet getUserPlanet(long userId);

    List<GalaxyTile> getTakenPositions();

    List<Planet> getPlanets(GalaxyRegion galaxyRegion);

    List<ResourcePlanet> findResourcePlanets();

    List<ResourcePlanet> findResourcePlanets(int corpId);

    List<UserPlanet> getUserPlanets(GalaxyRegion galaxyRegion);
    List<HumaroidPlanet> getHumaroidPlanet();
}

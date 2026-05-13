package com.go2super.service.jobs.other;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.sub.ResourcePlanet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.service.GalaxyService;
import com.go2super.service.JobService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.*;
import java.util.concurrent.*;

public class RBPJob implements OfflineJob {

    @Override
    public void setup() {

    }

    @Override
    public void run() {

        List<ResourcePlanet> resourcePlanets = GalaxyService.getInstance().getPlanetCache().findResourcePlanets();
        if (resourcePlanets.isEmpty()) {
            return;
        }

        CopyOnWriteArrayList<String> toUpdate = new CopyOnWriteArrayList<>();

        for (ResourcePlanet resourcePlanet : resourcePlanets) {

            if (resourcePlanet.getStatusTime() == null) {

                toUpdate.add(resourcePlanet.getId().toString());
                continue;

            }

            if (resourcePlanet.getStatusTime().before(DateUtil.now())) {

                toUpdate.add(resourcePlanet.getId().toString());
                continue;

            }

        }

        if (!toUpdate.isEmpty()) {

            List<Planet> toUpdatePlanets = GalaxyService.getInstance().getPlanetCache().findById(toUpdate);

            for (Planet planet : toUpdatePlanets) {

                if (planet == null || planet.getType() != PlanetType.RESOURCES_PLANET) {
                    continue;
                }
                ResourcePlanet resourcePlanet = (ResourcePlanet) planet;

                if (resourcePlanet.getStatusTime() == null) {

                    resourcePlanet.setPeace(false);
                    resourcePlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getRBPWarTime()));
                    GalaxyService.getInstance().getPlanetCache().save(resourcePlanet);
                    continue;

                }

                if (resourcePlanet.getStatusTime().before(DateUtil.now())) {

                    if (resourcePlanet.isPeace()) {

                        resourcePlanet.setPeace(false);
                        resourcePlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getRBPWarTime()));
                        GalaxyService.getInstance().getPlanetCache().save(resourcePlanet);
                        continue;

                    }

                    for (Fleet pirate : resourcePlanet.getFleets()) {
                        if (pirate.getGuid() == -1) {
                            pirate.remove();
                        }
                    }

                    resourcePlanet.setPeace(true);
                    resourcePlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getRBPPeaceTime()));
                    GalaxyService.getInstance().getPlanetCache().save(resourcePlanet);
                    continue;

                }

            }

        }

    }

    @Override
    public long getInterval() {

        return 900L;
    }

}

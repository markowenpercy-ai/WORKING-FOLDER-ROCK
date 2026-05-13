package com.go2super.service.jobs.other;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.sub.HumaroidPlanet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.GalaxyZone;
import com.go2super.service.GalaxyService;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class HumaroidJob implements OfflineJob {

    @Override
    public void setup() {

        List<HumaroidPlanet> humaroidPlanets = GalaxyService.getInstance().getPlanetCache().findHumaroidPlanets();

        for (int zone = 0; zone < GalaxyService.getInstance().getCurrentZones(); zone++) {

            GalaxyZone galaxyZone = new GalaxyZone(zone);
            List<HumaroidPlanet> zoneHumaroids = humaroidPlanets.stream()
                    .filter(humaroidPlanet -> galaxyZone.contains(humaroidPlanet.getPosition()))
                    .collect(Collectors.toList());
            Collections.shuffle(zoneHumaroids);

            for (int humaId = 0; humaId < zoneHumaroids.size(); humaId++) {

                HumaroidPlanet humaroidPlanet = zoneHumaroids.get(humaId);

                if (humaId < 6) {

                    humaroidPlanet.setPeace(false);
                    humaroidPlanet.setDestroyed(false);

                    humaroidPlanet.setCurrentLevel(GalaxyService.getInstance().getHumaroidLevel());
                    humaroidPlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getHumaroidWarTime()));
                    continue;

                }

                humaroidPlanet.setPeace(true);
                humaroidPlanet.setDestroyed(true);
                humaroidPlanet.setStatusTime(null);

            }

        }

    }

    @Override
    public void run() {

        List<HumaroidPlanet> humaroidPlanets = GalaxyService.getInstance().getPlanetCache().findHumaroidPlanets();
        if (humaroidPlanets.isEmpty()) {
            return;
        }

        CopyOnWriteArrayList<String> toUpdate = new CopyOnWriteArrayList<>();

        for (HumaroidPlanet humaroidPlanet : humaroidPlanets) {

            if (humaroidPlanet.getStatusTime() == null) {
                continue;
            }

            if (humaroidPlanet.getStatusTime().before(DateUtil.now())) {

                toUpdate.add(humaroidPlanet.getId().toString());

            }

        }

        if (!toUpdate.isEmpty()) {

            List<Planet> toUpdatePlanets = GalaxyService.getInstance().getPlanetCache().findById(toUpdate);

            for (Planet planet : toUpdatePlanets) {

                if (planet == null || planet.getType() != PlanetType.HUMAROID_PLANET) {
                    continue;
                }
                HumaroidPlanet humaroidPlanet = (HumaroidPlanet) planet;

                if (humaroidPlanet.getStatusTime() == null) {

                    humaroidPlanet.setPeace(false);
                    humaroidPlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getHumaroidWarTime()));
                    GalaxyService.getInstance().getPlanetCache().save(humaroidPlanet);
                    continue;

                }

                if (humaroidPlanet.getStatusTime().before(DateUtil.now())) {

                    if (humaroidPlanet.hasFight()) {
                        continue;
                    }

                    for (Fleet pirate : humaroidPlanet.getFleets()) {
                        if (pirate.getGuid() == -1) {
                            pirate.remove();
                        }
                    }

                    if (humaroidPlanet.isDestroyed()) {

                        GalaxyTile currentTile = humaroidPlanet.getPosition();
                        GalaxyZone currentZone = currentTile.getParentZone();

                        List<HumaroidPlanet> zoneHumaroids = humaroidPlanets.stream()
                                .filter(anotherPlanet -> currentZone.contains(humaroidPlanet.getPosition()))
                                .collect(Collectors.toList());
                        Collections.shuffle(zoneHumaroids);

                        for (HumaroidPlanet anotherHuma : zoneHumaroids) {
                            if (!anotherHuma.getPosition().equals(currentTile) && anotherHuma.isDestroyed() && anotherHuma.getStatusTime() == null) {

                                anotherHuma.setPeace(false);
                                anotherHuma.setDestroyed(false);

                                anotherHuma.setCurrentLevel(GalaxyService.getInstance().getHumaroidLevel());
                                anotherHuma.setStatusTime(DateUtil.now(GalaxyService.getInstance().getHumaroidWarTime()));

                                humaroidPlanet.setPeace(true);
                                humaroidPlanet.setDestroyed(true);
                                humaroidPlanet.setStatusTime(null);

                                GalaxyService.getInstance().getPlanetCache().save(anotherHuma);
                                GalaxyService.getInstance().getPlanetCache().save(humaroidPlanet);
                                break;

                            }
                        }

                    } else {

                        humaroidPlanet.setPeace(false);
                        humaroidPlanet.setDestroyed(true);
                        humaroidPlanet.setStatusTime(DateUtil.now(GalaxyService.getInstance().getHumaroidRuinTime()));

                        GalaxyService.getInstance().getPlanetCache().save(humaroidPlanet);

                    }

                }

            }

        }

    }

    @Override
    public long getInterval() {

        return 900L;
    }

}

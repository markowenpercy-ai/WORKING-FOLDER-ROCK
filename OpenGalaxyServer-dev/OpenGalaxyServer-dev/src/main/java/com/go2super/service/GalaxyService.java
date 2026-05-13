package com.go2super.service;

import com.go2super.database.cache.PlanetCache;
import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.MapArea;
import com.go2super.obj.type.BonusType;
import com.go2super.obj.utility.*;
import com.go2super.packet.fight.ResponseFightGalaxyBeginPacket;
import com.go2super.packet.map.ResponseMapAreaPacket;
import com.go2super.packet.ship.ResponseGalaxyShipPacket;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.GalaxyMapData;
import com.go2super.resources.data.InstanceData;
import com.go2super.resources.data.meta.EnemyFortificationMeta;
import com.go2super.resources.json.GalaxyMapJson;
import com.go2super.socket.util.DateUtil;
import com.go2super.socket.util.MathUtil;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.*;

@Getter
@Service
public class GalaxyService {

    public static final List<Integer> availableRBP = IntStream.rangeClosed(1, 55).boxed().collect(Collectors.toList());

    private static final boolean dummies = false;
    private static GalaxyService instance;

    private final int currentZones = 210;

    private final List<GalaxyTile> possiblePlanetPositions;

    private final List<GalaxyTile> possibleHumaroidPositions;
    private final List<GalaxyTile> possibleRBPPositions;

    @Getter
    private final PlanetCache planetCache;

    @Autowired
    public GalaxyService(PlanetCache planetCache) {

        instance = this;

        this.possiblePlanetPositions = new ArrayList<>();
        this.possibleHumaroidPositions = new ArrayList<>();
        this.possibleRBPPositions = new ArrayList<>();

        this.planetCache = planetCache;

    }

    public int getRBPPeaceTime() {

        if (PacketService.getInstance().isFastRbp()) {
            return 60 * 30;
        }
        return 60 * 60 * 72;
    }

    public int getRBPWarTime() {

        if (PacketService.getInstance().isFastRbp()) {
            return 60 * 30;
        }
        return 60 * 60 * 24;
    }

    public int getHumaroidWarTime() {

        if (PacketService.getInstance().isFastRbp()) {
            return 60 * 40;
        }
        return 60 * 60 * 4;
    }

    public int getHumaroidRuinTime() {

        if (PacketService.getInstance().isFastRbp()) {
            return 60 * 10;
        }
        return 60 * 60;
    }

    public int getHumaroidLevel() {

        return MathUtil.random(0, 15);
    }

    public ResponseFightGalaxyBeginPacket getUserPlanetGalaxyBeginPacket(Planet planet, int type, int pirateLevelId) {

        ResponseFightGalaxyBeginPacket packet = new ResponseFightGalaxyBeginPacket();

        packet.setKind(type);
        packet.setPirateLevelId(pirateLevelId);
        packet.setGalaxyId(planet.getPosition().galaxyId());

        return packet;

    }

    public UserPlanet getUserPlanet(long userId) {

        return planetCache.findUserPlanet(userId);
    }

    public UserPlanet getUserPlanet(User user) {

        return planetCache.findUserPlanet(user);
    }

    public void calculatePositions() {

        GalaxyMapJson planetsData = ResourceManager.getGalaxyMaps();

        for (int zone = 0; zone < currentZones; zone++) {

            GalaxyZone galaxyZone = new GalaxyZone(zone);

            for (GalaxyMapData mapData : planetsData.getPlayer()) {
                possiblePlanetPositions.add(mapData.getTile().offset(galaxyZone));
            }

            for (GalaxyMapData mapData : planetsData.getHumaroid()) {
                possibleHumaroidPositions.add(mapData.getTile().offset(galaxyZone));
            }

            for (GalaxyMapData mapData : planetsData.getCorpBonus()) {
                possibleRBPPositions.add(mapData.getTile().offset(galaxyZone));
            }

        }

    }

    public Planet getPlanet(GalaxyTile galaxyTile) {

        return planetCache.findByPosition(galaxyTile);
    }

    public boolean isAvailablePlanetPosition(GameCell cell) {

        Optional<GalaxyTile> result = possiblePlanetPositions.stream().filter(planet -> planet.getX() == cell.getX() && planet.getY() == cell.getY()).findAny();
        return result.isPresent();
    }

    public List<GalaxyTile> getAvailableTiles(int regionId) {

        return possiblePlanetPositions.stream().filter(tile -> tile.getParentRegion().regionId() == regionId).collect(Collectors.toList());
    }

    public List<GalaxyTile> getAvailableHumasTiles(int regionId) {

        return possibleHumaroidPositions.stream().filter(tile -> tile.getParentRegion().regionId() == regionId).collect(Collectors.toList());
    }

    public Optional<GalaxyTile> getAvailablePDRTile(int regionId) {

        return possibleRBPPositions.stream().filter(tile -> tile.getParentRegion().regionId() == regionId).findAny();
    }

    public GalaxyTile randomAvailablePosition() {

        List<GalaxyTile> takedTiles = planetCache.findTakenPositions();
        List<GalaxyTile> tiles = new ArrayList<>(possiblePlanetPositions);

        tiles.removeAll(takedTiles);
        Collections.shuffle(tiles);

        if (tiles.isEmpty()) {
            throw new RuntimeException("No available planet positions");
        }

        return tiles.get(0);

    }

    public List<MapArea> getDummyValues(int regionId, boolean some, List<Integer> excludeIds) {

        List<MapArea> mapAreas = new ArrayList<>();
        List<GalaxyTile> planets = getAvailableTiles(regionId);


        for (GalaxyTile planet : planets) {

            if (excludeIds.size() > 0) {
                if (excludeIds.contains(planet.galaxyId())) {
                    continue;
                }
            }

            if (dummies) {

                mapAreas.add(new MapArea(
                    "Dummy_Corp",
                    "Dummy" + MathUtil.random(0,999),
                    MathUtil.random(0, 2000000),
                    planet.galaxyId(),
                    0,
                    -1,
                    1,
                    MathUtil.random(0, 22),
                    MathUtil.random(0, 9),
                    some && MathUtil.random(1, 3) >= 2 ? 1 : 4,
                    0,
                    -1,
                    7));
                continue;

            }

            mapAreas.add(new MapArea(
                "",
                "",
                -1,
                planet.galaxyId(),
                0,
                -1,
                1,
                0,
                0,
                1,
                0,
                -1,
                -1));

        }

        return mapAreas;

    }

    public ResponseMapAreaPacket getMapAreaPacketByRegionId(User requester, int regionId) {

        GalaxyRegion region = new GalaxyRegion(regionId);
        List<Planet> planets = planetCache.findPlanets(region);
        List<Integer> excludeDummy = new ArrayList<>();

        for (Planet planet : planets) {
            if (planet instanceof UserPlanet) {
                excludeDummy.add(planet.getPosition().galaxyId());
            }
        }

        ResponseMapAreaPacket responseMapAreaPacket = new ResponseMapAreaPacket();
        List<MapArea> dummies = GalaxyService.getInstance().getDummyValues(regionId, true, excludeDummy);

        Corp requesterCorp = requester.getCorp();

        if (!planets.isEmpty()) {
            for (Planet planet : planets) {
                if (planet instanceof UserPlanet userPlanet) {

                    Optional<User> optionalUser = userPlanet.getUser();
                    if (optionalUser.isEmpty()) {
                        continue;
                    }

                    User user = optionalUser.get();

                    boolean hasFlag = false;
                    UserFlag userFlag = user.getFlag();

                    if (userFlag != null) {
                        Date until = userFlag.getUntil();
                        if (until != null && DateUtil.now().before(until)) {
                            hasFlag = true;
                        }
                    }

                    int camp;
                    int fight = 0;

                    List<UserBoost> truceBoosts = user.getStats().getUserBonus(BonusType.PLANET_PROTECTION);

                    if (!truceBoosts.isEmpty()) {
                        UserBoost userBoost = truceBoosts.get(0);
                        if (userBoost.getUntil() != null && userBoost.getUntil().after(new Date())) {
                            fight = 2;
                        }
                    } else if (userPlanet.isInWar()) {
                        fight = 1;
                    } else {
                        fight = 0;
                    }

                    Corp corp = CorpService.getInstance().getCorpCache().findByGuid(user.getGuid());

                    if (planet.getUserId() == requester.getUserId()) {
                        camp = 0;
                    } else if (corp != null && requesterCorp != null && corp.getCorpId() == requesterCorp.getCorpId()) {
                        camp = 1;
                    } else {
                        camp = 2;
                    }

                    MapArea mapArea = new MapArea(corp == null ? "" : corp.getName(),
                        user.getUsername(),
                        user.getUserId(),
                        planet.getPosition().galaxyId(),
                        0,
                        user.getStarFace(),
                        hasFlag ? 0 : 1,
                        corp == null ? -1 : corp.getIcon(),
                        corp == null ? -1 : corp.getLevel(),
                        user.getStarType(),
                        fight,
                        camp,
                        user.getSpaceStationLevel());

                    dummies.add(mapArea);

                } else if (planet instanceof HumaroidPlanet humaroid) {

                    if (humaroid.isDestroyed()) {

                        if (humaroid.getStatusTime() != null) {

                            dummies.add(new MapArea("",
                                "Humaroid",
                                humaroid.getUserId(),
                                humaroid.getPosition().galaxyId(),
                                0,
                                -1,
                                0,
                                0,
                                0,
                                2,
                                humaroid.hasFight() ? 1 : 0,
                                -1,
                                1));
                            continue;

                        }

                        dummies.add(new MapArea("",
                            "Unknown",
                            -1,
                            -1,
                            0,
                            -1,
                            1,
                            0,
                            0,
                            2,
                            0,
                            0,
                            0));
                        continue;

                    }

                    dummies.add(new MapArea("",
                        "Humaroid",
                        humaroid.getUserId(),
                        humaroid.getPosition().galaxyId(),
                        0,
                        -1,
                        1,
                        0,
                        0,
                        2,
                        humaroid.hasFight() ? 1 : 0,
                        -1,
                        1));

                } else if (planet instanceof ResourcePlanet rbp) {

                    if (!availableRBP.contains(region.getParentZone().zoneId() + 1)) {

                        MapArea mapArea = new MapArea("",
                            "Unknown",
                            -1,
                            -1,
                            0,
                            -1,
                            1,
                            0,
                            0,
                            5,
                            0,
                            0,
                            0);

                        dummies.add(mapArea);
                        continue;

                    }

                    Corp corp = CorpService.getInstance().getCorpCache().findByCorpId(rbp.getCurrentCorp());

                    int camp, fight;

                    if (corp == null) {
                        camp = 2;
                    } else if (corp != null && requesterCorp != null && corp.getCorpId() == requesterCorp.getCorpId()) {
                        camp = 1;
                    } else {
                        camp = 2;
                    }

                    if (rbp.isInWar()) {
                        fight = 1;
                    } else if (rbp.hasTruce()) {
                        fight = 2;
                    } else {
                        fight = 0;
                    }

                    MapArea mapArea = new MapArea(corp == null ? "" : corp.getName(),
                        "Resource Bonus Planet",
                        rbp.getUserId(),
                        rbp.getPosition().galaxyId(),
                        0,
                        -1,
                        1,
                        corp == null ? -1 : corp.getIcon(),
                        corp == null ? -1 : corp.getLevel(),
                        3,
                        fight,
                        camp,
                        1);

                    dummies.add(mapArea);

                }
            }
        }

        responseMapAreaPacket.setGalaxyMapId((byte) 0);
        responseMapAreaPacket.setRegionId(UnsignedShort.of(regionId));
        responseMapAreaPacket.setDataLen(UnsignedChar.of(dummies.size()));

        responseMapAreaPacket.setMapAreaList(dummies);

        return responseMapAreaPacket;

    }

    public ResponseGalaxyShipPacket getGalaxyShipInfo(int requester, int galaxyId, List<Fleet> fleets) {

        ResponseGalaxyShipPacket response = new ResponseGalaxyShipPacket();

        response.setGalaxyMapId((short) 0);

        response.setGalaxyId(galaxyId);
        response.setFleets(new ArrayList<>());

        for (Fleet fleet : fleets) {

            GalaxyFleetInfo fleetInfo = GalaxyFleetInfo.builder()
                .shipTeamId(fleet.getShipTeamId())
                .shipNum(fleet.ships())
                .bodyId((short) fleet.getBodyId())
                .reserve((short) 0)
                .direction((byte) fleet.getDirection())
                .posX((byte) fleet.getPosX())
                .posY((byte) fleet.getPosY())
                .owner((byte) (requester == fleet.getGuid() ? 2 : 0))
                .build();

            response.getFleets().add(fleetInfo);

        }

        response.setDataLen((short) response.getFleets().size());
        return response;

    }

    public ResponseGalaxyShipPacket getGalaxyShipInfo(User user, List<Fleet> fleets) {

        ResponseGalaxyShipPacket response = new ResponseGalaxyShipPacket();

        response.setGalaxyMapId((short) 0);

        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        response.setFleets(new ArrayList<>());

        for (Fleet fleet : fleets) {

            GalaxyFleetInfo fleetInfo = GalaxyFleetInfo.builder()
                .shipTeamId(fleet.getShipTeamId())
                .shipNum(fleet.ships())
                .bodyId((short) fleet.getBodyId())
                .reserve((short) 0)
                .direction((byte) fleet.getDirection())
                .posX((byte) fleet.getPosX())
                .posY((byte) fleet.getPosY())
                .owner((byte) (user.getGuid() == fleet.getGuid() ? 2 : 0))
                .build();

            response.getFleets().add(fleetInfo);

        }

        response.setDataLen((short) response.getFleets().size());
        return response;

    }

    public LinkedList<RBPBuilding> createRBPBuildings() {

        LinkedList<RBPBuilding> buildings = new LinkedList<>();
        InstanceData rbpData = ResourceManager.getRBP().getInstance();

        int index = 0;

        for (EnemyFortificationMeta meta : rbpData.getFortifications()) {

            RBPBuilding rbpBuilding = RBPBuilding.builder()
                .index(index++)
                .buildingId(meta.getId())
                .levelId(meta.getLevel())
                .x(meta.getX())
                .y(meta.getY())
                .repairing(false)
                .updating(false)
                .build();

            buildings.add(rbpBuilding);

        }

        return buildings;

    }

    public boolean isValidPlanetPosition(GalaxyTile position) {

        return possiblePlanetPositions.stream().anyMatch(galaxyTile -> galaxyTile.equals(position));
    }

    public static GalaxyService getInstance() {

        return instance;
    }

}

package com.go2super.service.jobs.other;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.*;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.game.JumpShipTeamInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.type.JumpType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.GameCell;
import com.go2super.packet.PacketRouter;
import com.go2super.packet.fight.ResponseFightGalaxyBeginPacket;
import com.go2super.packet.ship.ResponseCreateShipTeamPacket;
import com.go2super.packet.ship.ResponseJumpShipTeamPacket;
import com.go2super.service.*;
import com.go2super.service.battle.MatchRunnable;
import com.go2super.service.battle.match.WarMatch;
import com.go2super.service.jobs.OfflineJob;
import com.go2super.socket.util.DateUtil;

import java.util.*;
import java.util.concurrent.*;

public class TransitionJob implements OfflineJob {

    @Override
    public void setup() {

    }

    public void attackPlanet(Planet planet, FleetTransmission transmission, Fleet fleet, User owner, GalaxyTile from, GalaxyTile to, List<Fleet> initiators) {

        if (transmission.getJumpType() == JumpType.RECALL || transmission.getGalaxyId() == owner.getGalaxyId()) {

            // todo

        } else if (transmission.getJumpType() == JumpType.ATTACK) {

            fleet.setFleetInitiator(FleetInitiator.builder()
                .jumpType(JumpType.ATTACK)
                .build());

            // * Initialize the war
            if (!planet.isInWar()) {
                initiators.add(fleet);
            }

        } else if (transmission.getJumpType() == JumpType.DEFEND) {

            fleet.setFleetInitiator(FleetInitiator.builder()
                .jumpType(JumpType.DEFEND)
                .build());

        }

        GameCell spaceEntrance = GameCell.of(0, 0);
        if (planet.getType() == PlanetType.USER_PLANET) {
            spaceEntrance = to.getEntrance(from);
        }

        fleet.setGalaxyId(to.galaxyId());
        fleet.setPosX(spaceEntrance.getX());
        fleet.setPosY(spaceEntrance.getY());
        fleet.setFleetTransmission(null);

        ResponseCreateShipTeamPacket response = new ResponseCreateShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(to.galaxyId());

        for (LoggedGameUser viewer : LoginService.getInstance().getPlanetViewers(to.galaxyId())) {

            GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

            fleetInfo.setShipTeamId(fleet.getShipTeamId());
            fleetInfo.setShipNum(fleet.ships());
            fleetInfo.setBodyId((short) fleet.getBodyId());
            fleetInfo.setReserve((short) 0);
            fleetInfo.setDirection((byte) fleet.getDirection());

            fleetInfo.setPosX((byte) fleet.getPosX());
            fleetInfo.setPosY((byte) fleet.getPosY());
            fleetInfo.setOwner((byte) (BattleService.getInstance().getFleetColor(viewer, fleet)));

            response.setGalaxyFleetInfo(fleetInfo);
            viewer.getSmartServer().send(response);

        }

        fleet.save();

    }

    @Override
    public void run() {

        List<Fleet> transitionFleets = PacketService.getInstance().getFleetCache().getInTransmissionFleets();
        if (transitionFleets.isEmpty()) {
            return;
        }

        List<Fleet> initiators = new ArrayList<>();
        CopyOnWriteArrayList<Integer> toUpdate = new CopyOnWriteArrayList<>();

        for (Fleet fleet : transitionFleets) {

            FleetTransmission transmission = fleet.getFleetTransmission();
            if (transmission == null) {
                continue;
            }

            if (DateUtil.remains(transmission.getUntil()).intValue() <= 0) {
                toUpdate.add(fleet.getShipTeamId());
            }

        }

        if (!toUpdate.isEmpty()) {

            List<Fleet> toUpdateFleets = PacketService.getInstance().getFleetCache().findByShipTeamId(toUpdate);

            for (Fleet fleet : toUpdateFleets) {

                FleetTransmission transmission = fleet.getFleetTransmission();
                if (transmission == null) {
                    continue;
                }

                User owner = fleet.getUser();

                GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());
                GalaxyTile to = new GalaxyTile(transmission.getGalaxyId());

                Planet planet = GalaxyService.getInstance().getPlanet(to);
                boolean invalid = false;

                if (planet == null) {
                    invalid = true;
                } else if (List.of(PlanetType.HUMAROID_PLANET).contains(planet.getType())) {
                    HumaroidPlanet humaroidPlanet = (HumaroidPlanet) planet;
                    if (humaroidPlanet.hasTruce() || humaroidPlanet.isInWar()) {
                        invalid = true;
                    }
                } else if (planet.getType() == PlanetType.USER_PLANET) {
                    UserPlanet userPlanet = (UserPlanet) planet;
                    Optional<User> optionalUser = userPlanet.getUser();
                    if (optionalUser.isPresent() && optionalUser.get().getStats().hasTruce()) {
                        User targetUser = optionalUser.get();
                        if (targetUser.getGuid() != owner.getGuid()) {
                            if (targetUser.getConsortiaId() == -1) {
                                invalid = true;
                            } else if (targetUser.getConsortiaId() != owner.getConsortiaId()) {
                                invalid = true;
                            }
                        }
                    }
                } else if (planet.getType() == PlanetType.RESOURCES_PLANET) {
                    ResourcePlanet resourcePlanet = (ResourcePlanet) planet;
                    if (resourcePlanet.hasTruce()) {
                        invalid = true;
                    }
                }

                if (invalid) {

                    int remains = DateUtil.remains(transmission.getUntil()).intValue();
                    int spare = transmission.getTotal() - remains;

                    Date until = DateUtil.now(spare);

                    fleet.setGalaxyId(transmission.getGalaxyId());
                    fleet.setFleetTransmission(FleetTransmission.builder()
                            .galaxyId(owner.getGalaxyId())
                            .jumpType(JumpType.RECALL)
                            .total(spare)
                            .until(until)
                            .build());

                    fleet.save();

                    ResponseJumpShipTeamPacket response = new ResponseJumpShipTeamPacket();
                    response.setData(JumpShipTeamInfo.builder()
                            .userId(owner.getUserId())
                            .userName(owner.getUsername())
                            .shipTeamId(fleet.getShipTeamId())
                            .fromGalaxyId(fleet.getGalaxyId())
                            .toGalaxyId(owner.getGalaxyId())
                            .spareTime(spare)
                            .totalTime(spare)
                            .fromGalaxyMapId(0)
                            .toGalaxyMapId(0)
                            .kind((byte) 0)
                            .galaxyType((byte) 1)
                            .build());

                    Optional<LoggedGameUser> optionalLoggedGameUser = owner.getLoggedGameUser();
                    if (optionalLoggedGameUser.isPresent()) {
                        optionalLoggedGameUser.get().getSmartServer().send(response);
                    }
                    continue;

                }

                attackPlanet(planet, transmission, fleet, owner, from, to, initiators);
                continue;

            }

            List<WarMatch> makeWars = new ArrayList<>();

            for (Fleet initiator : initiators) {

                if (makeWars.stream().anyMatch(war -> war.getGalaxyId() == initiator.getGalaxyId())) {
                    continue;
                }
                Planet planet = GalaxyService.getInstance().getPlanet(new GalaxyTile(initiator.getGalaxyId()));

                if (planet == null) {
                    continue;
                }

                MatchRunnable runnable = BattleService.getInstance().makeWarMatch(planet);
                WarMatch warMatch = (WarMatch) runnable.getMatch();

                ResponseFightGalaxyBeginPacket packet = GalaxyService.getInstance()
                        .getUserPlanetGalaxyBeginPacket(planet, planet.getType().getMsgId(), 0);
                PacketRouter.getInstance().broadcast(packet);

                warMatch.start();
                makeWars.add(warMatch);

            }
        }

    }


    @Override
    public long getInterval() {

        return 900L;
    }

}

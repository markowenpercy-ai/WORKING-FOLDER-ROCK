package com.go2super.listener;

import com.go2super.database.entity.Corp;
import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.Planet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserInventory;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.game.MapArea;
import com.go2super.obj.game.Prop;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.map.ResponseMapAreaPacket;
import com.go2super.packet.planet.RequestMoveHomePacket;
import com.go2super.packet.planet.ResponseMoveHomeBroPacket;
import com.go2super.packet.planet.ResponseMoveHomePacket;
import com.go2super.service.*;
import com.go2super.service.exception.BadGuidException;
import com.go2super.service.jobs.other.DefendJob;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

import static com.go2super.service.battle.match.WarMatch.returnFleets;

public class PlanetListener implements PacketListener {

    @PacketProcessor
    public void onMoveHome(RequestMoveHomePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        if (DefendJob.getInstance().getCurrentFlag() == user.getGuid()) {

            user.sendWarning("<strong><font face=\"Verdana\" color='#ffa500'>You can't use this while defending!</font></strong>");
            return;

        }

        UserInventory userInventory = UserService.getInstance().getUserCache().findByGuid(packet.getGuid()).getInventory();
        Prop prop = userInventory.getProp(901);
        if (prop == null) {
            return;
        }

        if (PacketService.getInstance().hasFleetsDeployed(user)) {

            packet.reply(ResponseMoveHomePacket.builder()
                    .consortiaName(SmartString.of("", 32))
                    .errorCode(1)
                    .toGalaxyId(-1)
                    .toGalaxyMapId(0)
                    .propsId(prop.getPropId())
                    .lockFlag(0)
                    .build());

            return;

        }

        GalaxyTile position = new GalaxyTile(packet.getToGalaxyId());
        UserPlanet planet = GalaxyService.getInstance().getUserPlanet(user);

        if (planet.isInWar()) {
            return;
        }
        if (!GalaxyService.getInstance().isValidPlanetPosition(position)) {
            return;
        }

        Planet userPlanetGalaxyTile = GalaxyService.getInstance().getPlanet(position);
        int fromGalaxyId = planet.getPosition().galaxyId();

        if (userPlanetGalaxyTile != null) {

            packet.reply(ResponseMoveHomePacket.builder()
                    .consortiaName(SmartString.of("", 32))
                    .errorCode(1)
                    .toGalaxyId(planet.getPosition().galaxyId())
                    .toGalaxyMapId(0)
                    .propsId(prop.getPropId())
                    .lockFlag(0)
                    .build());

            return;

        }

        Pair<Boolean, Boolean> propRemove = user.getInventory().removeProp(prop, 1);

        if (!propRemove.getKey()) // * ¿Se removió?
        {
            return;
        }

        planet.setPosition(position);

        int regionId = planet.getPosition().getParentRegion().regionId();
        int galaxyId = planet.getPosition().galaxyId();

        String name = CorpService.getCorpName(user.getGuid());
        Corp corp = CorpService.getInstance().getCorpByUser(user.getGuid());

        ResponseMoveHomeBroPacket responseToMoveHomeBroPacket = ResponseMoveHomeBroPacket.builder()
                .delGalaxyId(fromGalaxyId)
                .mapArea(new MapArea(name,
                        user.getUsername(),
                        user.getUserId(),
                        galaxyId,
                        0,
                        user.getStarFace(),
                        -1,
                        corp == null ? -1 : corp.getIcon(),
                        corp == null ? -1 : corp.getCorpId(),
                        user.getStarType(),
                        0,
                        -1,
                        user.getSpaceStationLevel()))
                .build();

        ResponseMoveHomePacket responseMoveHome = ResponseMoveHomePacket.builder()
                .consortiaName(SmartString.of(name, 32))
                .errorCode(0)
                .toGalaxyId(planet.getPosition().galaxyId())
                .toGalaxyMapId(0)
                .propsId(prop.getPropId())
                .lockFlag(propRemove.getValue() ? 1 : 0) // ¿Locked prop?
                .build();

        ResponseMapAreaPacket responseMapAreaPacket = GalaxyService.getInstance().getMapAreaPacketByRegionId(user, regionId);

        GalaxyService.getInstance().getPlanetCache().save(planet);
        user.update();
        user.save();

        for (Fleet fleet : PacketService.getInstance().getFleetCache().findAllByGuid(user.getGuid())) {
            if (fleet.getGalaxyId() == fromGalaxyId) {

                fleet.setGalaxyId(position.galaxyId());
                fleet.save();

            }
        }

        List<Fleet> toReturn = new ArrayList<>();
        for (Fleet fleet : PacketService.getInstance().getFleetCache().findAllByGalaxyId(fromGalaxyId)) {
            if (fleet.getGuid() == user.getGuid() && fleet.getGalaxyId() == fromGalaxyId) {
                fleet.setGalaxyId(position.galaxyId());
                fleet.save();
                continue;
            }
            toReturn.add(fleet);
        }
        returnFleets(toReturn);

        packet.reply(responseMoveHome, responseMapAreaPacket);

        for (LoggedGameUser loggedGameUser : LoginService.getInstance().getGameUsers()) { // usuarios en linea

            // loggedGameUser.getSmartServer().send(responseFromMoveHomeBroPacket);
            loggedGameUser.getSmartServer().send(responseToMoveHomeBroPacket);

        }
    }
}

package com.go2super.listener;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserPlanet;
import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.obj.model.LoggedGameUser;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.ship.ResponseCreateShipTeamPacket;
import com.go2super.packet.ship.ResponseDeleteShipTeamBroadcastPacket;
import com.go2super.packet.star.RequestFromResourceStarToHomePacket;
import com.go2super.service.BattleService;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

import java.util.*;

public class StarListener implements PacketListener {

    @PacketProcessor
    public void onReturnHome(RequestFromResourceStarToHomePacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        UserPlanet planet = user.getPlanet();
        if (planet == null) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null || fleet.getGuid() != packet.getGuid() || fleet.isInTransmission() || fleet.isInMatch()) {
            return;
        }

        GalaxyTile targetTile = planet.getPosition();
        GalaxyTile from = new GalaxyTile(fleet.getGalaxyId());

        fleet.setGalaxyId(targetTile.galaxyId());
        fleet.setFleetTransmission(null);
        fleet.setFleetInitiator(null);

        ResponseCreateShipTeamPacket response = new ResponseCreateShipTeamPacket();

        response.setGalaxyMapId(0);
        response.setGalaxyId(targetTile.galaxyId());

        GalaxyFleetInfo fleetInfo = new GalaxyFleetInfo();

        fleetInfo.setShipTeamId(fleet.getShipTeamId());
        fleetInfo.setShipNum(fleet.ships());
        fleetInfo.setBodyId((short) fleet.getBodyId());
        fleetInfo.setReserve((short) 0);
        fleetInfo.setDirection((byte) fleet.getDirection());

        fleetInfo.setPosX((byte) fleet.getPosX());
        fleetInfo.setPosY((byte) fleet.getPosY());

        response.setGalaxyFleetInfo(fleetInfo);

        for (LoggedGameUser viewer : LoginService.getInstance().getPlanetViewers(targetTile.galaxyId())) {

            fleetInfo.setOwner((byte) (BattleService.getInstance().getFleetColor(viewer, fleet)));
            viewer.getSmartServer().send(response);

        }

        fleet.save();

        List<LoggedGameUser> loggedGameUsers = LoginService.getInstance().getPlanetViewers(from.galaxyId());

        for (LoggedGameUser loggedGameUser : loggedGameUsers) {

            ResponseDeleteShipTeamBroadcastPacket broadcast = new ResponseDeleteShipTeamBroadcastPacket();

            broadcast.setGalaxyMapId(0);
            broadcast.setGalaxyId(targetTile.galaxyId());
            broadcast.setShipTeamId(fleet.getShipTeamId());

            loggedGameUser.getSmartServer().send(broadcast);

        }

    }

}

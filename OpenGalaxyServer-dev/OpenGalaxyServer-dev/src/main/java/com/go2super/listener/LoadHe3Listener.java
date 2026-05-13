package com.go2super.listener;

import com.go2super.database.entity.Fleet;
import com.go2super.database.entity.User;
import com.go2super.obj.game.ShipHe3Info;
import com.go2super.packet.PacketListener;
import com.go2super.packet.PacketProcessor;
import com.go2super.packet.loadhe3.*;
import com.go2super.service.LoginService;
import com.go2super.service.PacketService;
import com.go2super.service.UserService;
import com.go2super.service.exception.BadGuidException;

import java.util.*;

public class LoadHe3Listener implements PacketListener {

    private final int MAX_RESOURCE = 900000000;

    @PacketProcessor
    public void onLoadShipTeamAll(RequestLoadShipTeamAllPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());
        if (user == null) {
            return;
        }

        List<Fleet> fleets = user.getFleets();

        List<ResponseLoadShipTeamAllPacket> responsePackets = new ArrayList<>();
        ResponseLoadShipTeamAllPacket currentResponse = null;

        int index = 0;

        for (Fleet fleet : fleets) {

            if (fleet.isInMatch() || fleet.isInTransmission()) {
                continue;
            }

            if (++index > 18) {

                responsePackets.add(currentResponse);
                currentResponse = null;

            }

            if (currentResponse == null) {

                index = 0;
                currentResponse = new ResponseLoadShipTeamAllPacket();

                currentResponse.setSeqId(packet.getSeqId() + 1);
                currentResponse.setGuid(packet.getGuid());

            }

            ShipHe3Info ship = new ShipHe3Info();

            ship.getShipName().value(fleet.getName());

            ship.setGas(fleet.getHe3());
            ship.setShipSpace(fleet.getMaxHe3());

            ship.setShipNum(fleet.ships());
            ship.setShipTeamId(fleet.getShipTeamId());

            ship.setSupply(fleet.getSupply()); // need to change
            currentResponse.getShips().add(ship);

            currentResponse.setDataLen(currentResponse.getShips().size());

        }

        if (currentResponse != null) {
            responsePackets.add(currentResponse);
        }
        packet.reply(responsePackets);

    }

    @PacketProcessor
    public void onLoadShipTeam(RequestLoadShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }
        if (packet.getGas().getValue() <= 0 || packet.getGas().getValue() > MAX_RESOURCE) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null || fleet.getGuid() != user.getGuid() || fleet.isInTransmission() || fleet.isInMatch()) {
            return;
        }

        int value = packet.getGas().getValue();
        int newValue = fleet.getHe3() + value;

        if (newValue < 0 || newValue > fleet.getMaxHe3()) {
            return;
        }
        if (user.getResources().getHe3() - value < 0) {
            return;
        }

        user.getResources().setHe3(user.getResources().getHe3() - value);

        fleet.setHe3(newValue);
        fleet.save();

        user.getMetrics().add("action:load.fleet", 1);
        user.update();
        user.save(); // todo changes

        ResponseLoadShipTeamPacket response = new ResponseLoadShipTeamPacket();

        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        response.setShipTeamId(packet.getShipTeamId());
        response.setGas(packet.getGas());

        packet.reply(response);

    }

    @PacketProcessor
    public void onUnloadShipTeam(RequestUnloadShipTeamPacket packet) throws BadGuidException {

        LoginService.validate(packet, packet.getGuid());

        User user = UserService.getInstance().getUserCache().findByGuid(packet.getGuid());

        if (user == null) {
            return;
        }
        if (packet.getGas().getValue() <= 0 || packet.getGas().getValue() > MAX_RESOURCE) {
            return;
        }

        Fleet fleet = PacketService.getInstance().getFleetCache().findByShipTeamId(packet.getShipTeamId());
        if (fleet == null || fleet.getGuid() != user.getGuid() || fleet.isInTransmission() || fleet.isInMatch()) {
            return;
        }

        int value = packet.getGas().getValue();
        int newValue = fleet.getHe3() - value;
        if (newValue < 0 || newValue > fleet.getMaxHe3()) {
            return;
        }

        user.getResources().addHe3(value);

        fleet.setHe3(newValue);
        fleet.save();
        user.save(); // todo changes

        ResponseUnloadShipTeamPacket response = new ResponseUnloadShipTeamPacket();

        response.setGalaxyId(user.getPlanet().getPosition().galaxyId());
        response.setShipTeamId(packet.getShipTeamId());
        response.setGas(packet.getGas());

        packet.reply(response);

    }

}

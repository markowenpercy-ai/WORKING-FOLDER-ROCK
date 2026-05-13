package com.go2super.packet.ship;

import com.go2super.obj.game.GalaxyFleetInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseGalaxyShipPacket extends Packet {

    public static final int TYPE = 1336;

    private short dataLen;

    private short galaxyMapId;
    private int galaxyId;

    private List<GalaxyFleetInfo> fleets = new ArrayList<>();

}

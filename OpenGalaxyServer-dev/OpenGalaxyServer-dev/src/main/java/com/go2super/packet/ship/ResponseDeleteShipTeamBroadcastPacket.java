package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseDeleteShipTeamBroadcastPacket extends Packet {

    public static final int TYPE = 1341;

    private int galaxyMapId;
    private int galaxyId;

    private int shipTeamId;

}

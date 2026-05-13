package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseMoveShipTeamPacket extends Packet {

    public static final int TYPE = 1323;

    private int galaxyMapId;
    private int galaxyId;

    private int shipTeamId;

    private int posX;
    private int posY;

}

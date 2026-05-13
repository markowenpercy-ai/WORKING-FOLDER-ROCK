package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseDirectionShipTeamPacket extends Packet {

    public static final int TYPE = 1339;

    private int galaxyMapId;
    private int galaxyId;

    private int shipTeamId;
    private byte direction;

}

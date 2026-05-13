package com.go2super.packet.loadhe3;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUnloadShipTeamPacket extends Packet {

    public static final int TYPE = 1333;

    private int galaxyMapId;
    private int galaxyId;

    private int shipTeamId;
    private UnsignedInteger gas = new UnsignedInteger(0);

}

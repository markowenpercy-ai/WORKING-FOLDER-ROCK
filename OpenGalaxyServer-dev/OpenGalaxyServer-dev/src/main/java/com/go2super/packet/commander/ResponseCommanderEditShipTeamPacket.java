package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCommanderEditShipTeamPacket extends Packet {

    public static final int TYPE = 1518;

    private int errorCode;
    private byte kind;

}

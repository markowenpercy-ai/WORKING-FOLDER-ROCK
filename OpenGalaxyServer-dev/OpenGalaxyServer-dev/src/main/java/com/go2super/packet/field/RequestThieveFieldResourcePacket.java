package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestThieveFieldResourcePacket extends Packet {

    public static final int TYPE = 1806;

    private int seqId;
    private int guid;

    private int objGuid;
    private int objGalaxyId;

}

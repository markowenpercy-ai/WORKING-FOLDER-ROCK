package com.go2super.packet.planet;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestMoveHomePacket extends Packet {

    public static final int TYPE = 1110;

    private int seqId;
    private int guid;

    private int toGalaxyMapId;
    private int toGalaxyId;

}

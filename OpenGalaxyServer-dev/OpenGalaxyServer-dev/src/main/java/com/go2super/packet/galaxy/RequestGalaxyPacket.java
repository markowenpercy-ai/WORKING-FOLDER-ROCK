package com.go2super.packet.galaxy;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestGalaxyPacket extends Packet {

    public static final int TYPE = 1100;

    private int seqId;
    private int guid;

    private int galaxyMapId;
    private int galaxyId;

}

package com.go2super.packet.map;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestMapBlockPacket extends Packet {

    public static final int TYPE = 1255;

    private int seqId;
    private int guid;
    private int galaxyMapId;
    private int blockId;

}

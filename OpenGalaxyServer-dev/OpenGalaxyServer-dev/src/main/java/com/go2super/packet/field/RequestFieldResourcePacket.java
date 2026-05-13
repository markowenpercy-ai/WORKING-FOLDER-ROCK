package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFieldResourcePacket extends Packet {

    public static final int TYPE = 1800;

    private int seqId;
    private int guid;

    private int galaxyMapId;
    private int galaxyId;

}

package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestGrowFieldResourcePacket extends Packet {

    public static final int TYPE = 1802;

    private int seqId;
    private int guid;

    private int galaxyId;
    private int resourceId;

}

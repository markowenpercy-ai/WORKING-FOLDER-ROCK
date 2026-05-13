package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDeleteFieldResourcePacket extends Packet {

    public static final int TYPE = 1808;

    private int seqId;
    private int guid;

    private int galaxyId;

}

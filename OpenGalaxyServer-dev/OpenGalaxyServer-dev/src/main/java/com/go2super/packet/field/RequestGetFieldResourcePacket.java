package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestGetFieldResourcePacket extends Packet {

    public static final int TYPE = 1804;

    private int seqId;
    private int guid;

    private int galaxyId;

}

package com.go2super.packet;

import lombok.Data;

@Data
public class PacketHeartbeat extends Packet {

    public static final int TYPE = 1020;

    private int seqId;
    private int guid;

}

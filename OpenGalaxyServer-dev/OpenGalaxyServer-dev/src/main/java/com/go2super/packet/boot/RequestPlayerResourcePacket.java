package com.go2super.packet.boot;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestPlayerResourcePacket extends Packet {

    public static final int TYPE = 1213;

    private int seqId;
    private int guid;

}

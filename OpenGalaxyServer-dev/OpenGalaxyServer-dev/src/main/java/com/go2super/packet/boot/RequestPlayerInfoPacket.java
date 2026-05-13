package com.go2super.packet.boot;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestPlayerInfoPacket extends Packet {

    public static final int TYPE = 1017;

    private int seqId;
    private int guid;
    private int var3;

}

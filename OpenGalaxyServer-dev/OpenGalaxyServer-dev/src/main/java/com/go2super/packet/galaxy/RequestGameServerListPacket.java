package com.go2super.packet.galaxy;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestGameServerListPacket extends Packet {

    public static final int TYPE = 1071;

    private int seqId;
    private int guid;

}

package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaFieldPacket extends Packet {

    public static final int TYPE = 1580;

    private int seqId;
    private int guid;

}

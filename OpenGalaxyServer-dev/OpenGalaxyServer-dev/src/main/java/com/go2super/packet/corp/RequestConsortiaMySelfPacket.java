package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaMySelfPacket extends Packet {

    public static final int TYPE = 1556;

    private int seqId;
    private int guid;

}

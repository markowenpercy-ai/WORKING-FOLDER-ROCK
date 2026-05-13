package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestAddPackPacket extends Packet {

    public static final int TYPE = 1057;

    private int seqId;
    private int guid;

}

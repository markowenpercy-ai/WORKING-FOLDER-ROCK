package com.go2super.packet.props;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestTimeQueuePacket extends Packet {

    public static final int TYPE = 1224;

    private int seqId;
    private int guid;

}

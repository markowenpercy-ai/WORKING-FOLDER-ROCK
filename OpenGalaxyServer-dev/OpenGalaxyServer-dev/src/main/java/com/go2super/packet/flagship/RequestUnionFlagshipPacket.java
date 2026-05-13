package com.go2super.packet.flagship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUnionFlagshipPacket extends Packet {

    public static final int TYPE = 1376;

    private int seqId;
    private int guid;
    private int propsId;

}

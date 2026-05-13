package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUnbindCommanderCardPacket extends Packet {

    public static final int TYPE = 1537;

    private int seqId;
    private int guid;
    private int propsId;

}

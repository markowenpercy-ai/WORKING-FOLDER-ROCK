package com.go2super.packet.match;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestMatchPagePacket extends Packet {

    public static final int TYPE = 1448;

    private int seqId;
    private int guid;
    private int pageId;

}
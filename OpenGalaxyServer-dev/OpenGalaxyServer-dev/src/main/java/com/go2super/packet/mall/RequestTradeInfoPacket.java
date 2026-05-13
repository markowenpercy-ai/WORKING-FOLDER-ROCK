package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestTradeInfoPacket extends Packet {

    public static final int TYPE = 1756;

    private int seqId;
    private int guid;
    private int kind;
    private int id;
    private int pageId;

}
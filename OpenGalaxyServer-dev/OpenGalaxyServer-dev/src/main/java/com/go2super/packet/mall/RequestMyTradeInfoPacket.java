package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestMyTradeInfoPacket extends Packet {

    public static final int TYPE = 1752;

    private int seqId;
    private int guid;

}
package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDeleteTradeGoodsPacket extends Packet {

    public static final int TYPE = 1754;

    private int seqId;
    private int guid;

    private int indexId;

}
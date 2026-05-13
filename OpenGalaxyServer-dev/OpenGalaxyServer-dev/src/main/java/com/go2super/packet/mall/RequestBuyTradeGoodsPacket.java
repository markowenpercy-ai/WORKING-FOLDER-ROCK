package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestBuyTradeGoodsPacket extends Packet {

    public static final int TYPE = 1758;

    private int seqId;
    private int guid;
    private int sellGuid;
    private int indexId;

}

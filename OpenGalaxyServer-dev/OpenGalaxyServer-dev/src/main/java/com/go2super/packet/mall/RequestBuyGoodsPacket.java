package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestBuyGoodsPacket extends Packet {

    public static final int TYPE = 1062;

    private int seqId;
    private int guid;
    private int propsId;
    private int currency;
    private int num;

}
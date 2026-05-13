package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestTradeGoodsPacket extends Packet {

    public static final int TYPE = 1750;

    private int seqId;
    private int guid;

    private int id;
    private int num;
    private int price;

    private byte tradeType;
    private byte priceType;
    private byte timeType;
    private byte kind;

}
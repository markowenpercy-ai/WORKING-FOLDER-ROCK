package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseTradeGoodsPacket extends Packet {

    public static final int TYPE = 1751;

    private int errorCode;
    private int id;
    private int num;

    private byte tradeType;
    private byte kind;
    private byte priceType;
    private byte timeType;

    private int value;

}
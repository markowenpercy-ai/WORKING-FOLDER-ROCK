package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseBuyTradeGoodsPacket extends Packet {

    public static final int TYPE = 1759;

    private int errorCode;
    private int sellGuid;
    private int indexId;
    private int priceType;
    private int price;

}

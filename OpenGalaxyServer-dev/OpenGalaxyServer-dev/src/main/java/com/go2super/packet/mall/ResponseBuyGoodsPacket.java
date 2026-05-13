package com.go2super.packet.mall;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseBuyGoodsPacket extends Packet {

    public static final int TYPE = 1063;

    private int propsId;
    private int price;
    private int num;

    private byte lockFlag;
    private byte currency;

}
package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUnionCommanderCardPacket extends Packet {

    public static final int TYPE = 1523;

    private int propsId;

    private int card1;
    private int card2;
    private int card3;

    private int goods;
    private int goodsLockFlag;

}

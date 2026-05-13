package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestUnionCommanderCardPacket extends Packet {

    public static final int TYPE = 1522;

    private int seqId;
    private int guid;

    private int card1;
    private int card2;
    private int card3;

    private int goods;
    private int goodsLockFlag;

}

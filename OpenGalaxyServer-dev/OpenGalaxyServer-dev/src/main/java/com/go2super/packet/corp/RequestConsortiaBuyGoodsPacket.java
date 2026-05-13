package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaBuyGoodsPacket extends Packet {

    public static final int TYPE = 1593;

    private int seqId;
    private int guid;

    private int goodsId;
    private int num;

}

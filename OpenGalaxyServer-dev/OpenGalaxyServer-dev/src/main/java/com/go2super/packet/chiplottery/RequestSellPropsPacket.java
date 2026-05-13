package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestSellPropsPacket extends Packet {

    public static final int TYPE = 1908;

    private int seqId;
    private int guid;

    private int type;
    private int id;

    private int lockFlag;
    private int num;

}

package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCmosLotteryInfoPacket extends Packet {

    public static final int TYPE = 1902;

    private int seqId;
    private int guid;

}

package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestGainCmostLotteryPacket extends Packet {

    public static final int TYPE = 1900;

    private int seqId;
    private int guid;

    private int phaseId;
    private int type;

}

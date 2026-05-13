package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestRacingRankPacket extends Packet {
    public static final int TYPE = 1867;

    public int seqId;
    private long userId;
    private int guid;
    private int pageId;
}

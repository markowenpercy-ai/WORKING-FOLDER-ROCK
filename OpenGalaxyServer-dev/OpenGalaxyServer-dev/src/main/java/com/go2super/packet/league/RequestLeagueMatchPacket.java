package com.go2super.packet.league;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestLeagueMatchPacket extends Packet {
    public static final int TYPE = 1706;
    private int seqId;
    private int guid;
    private int pageId;
    private int objGuid;
}

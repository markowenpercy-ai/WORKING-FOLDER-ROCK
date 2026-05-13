package com.go2super.packet.rank;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestRankFightPacket extends Packet {

    public static final int TYPE = 1704;

    private int seqId;
    private int guid;

    private int pageId;

}

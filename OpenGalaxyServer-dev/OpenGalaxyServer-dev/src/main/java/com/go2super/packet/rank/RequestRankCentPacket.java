package com.go2super.packet.rank;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestRankCentPacket extends Packet {

    public static final int TYPE = 1700;

    private int seqId;
    private int guid;

    private int pageId;
    private int objGuid;

}

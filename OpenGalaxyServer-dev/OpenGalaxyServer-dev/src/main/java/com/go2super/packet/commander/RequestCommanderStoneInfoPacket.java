package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCommanderStoneInfoPacket extends Packet {

    public static final int TYPE = 1532;

    private int seqId;
    private int guid;
    private int commanderId;
    private int objGuid;

}

package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestClearCommanderPercentPacket extends Packet {

    public static final int TYPE = 1512;

    private int seqId;
    private int guid;
    private int commanderId;

}

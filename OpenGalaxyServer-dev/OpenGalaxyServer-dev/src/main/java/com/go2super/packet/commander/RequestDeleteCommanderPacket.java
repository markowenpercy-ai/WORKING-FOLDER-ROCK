package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDeleteCommanderPacket extends Packet {

    public static final int TYPE = 1504;

    private int seqId;
    private int guid;
    private int commanderId;

}

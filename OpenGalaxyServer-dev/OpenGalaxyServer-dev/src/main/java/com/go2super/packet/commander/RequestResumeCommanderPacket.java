package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestResumeCommanderPacket extends Packet {

    public static final int TYPE = 1510;

    private int seqId;
    private int guid;
    private int commanderId;

}

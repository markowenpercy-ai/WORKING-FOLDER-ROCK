package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestReliveCommanderPacket extends Packet {

    public static final int TYPE = 1508;

    private int seqId;
    private int guid;
    private int commanderId;

}

package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestSpeedTechPacket extends Packet {

    public static final int TYPE = 1228;

    private int seqId;
    private int guid;

    private int techId;
    private int techSpeedId;

    private int kind;

}

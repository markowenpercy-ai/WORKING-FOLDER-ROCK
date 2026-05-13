package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestSpeedBuildPacket extends Packet {

    public static final int TYPE = 1222;

    private int seqId;
    private int guid;

    private int indexId;
    private int buildingSpeedId;
    private int kind;

}

package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUnionCmosPacket extends Packet {

    public static final int TYPE = 1904;

    private int seqId;
    private int guid;

    private int cmosId1;
    private int cmosId2;

}

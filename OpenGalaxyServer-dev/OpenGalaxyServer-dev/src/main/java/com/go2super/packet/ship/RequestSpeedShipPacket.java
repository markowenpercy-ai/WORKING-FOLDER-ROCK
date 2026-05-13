package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestSpeedShipPacket extends Packet {

    public static final int TYPE = 1366;

    private int seqId;
    private int guid;

    private int indexId;


}

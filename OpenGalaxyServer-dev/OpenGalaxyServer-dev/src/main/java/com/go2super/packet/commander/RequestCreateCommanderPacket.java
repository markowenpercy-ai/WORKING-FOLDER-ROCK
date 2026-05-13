package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCreateCommanderPacket extends Packet {

    public static final int TYPE = 1500;

    private int seqId;
    private int guid;

    private byte kind;

}

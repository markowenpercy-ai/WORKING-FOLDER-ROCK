package com.go2super.packet.map;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseMapBlockPacket extends Packet {

    public static final int TYPE = 1061;

    private int blockCount;

}

package com.go2super.packet.fight;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseFightBoutBegPacket extends Packet {

    public static final int TYPE = 1413;

    private int galaxyMapId;
    private int galaxyId;
    private short boutId;

}
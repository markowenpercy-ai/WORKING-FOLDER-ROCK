package com.go2super.packet.map;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;


@Data
public class RequestMapAreaPacket extends Packet {

    public static final int TYPE = 1250;

    private int seqId;
    private int guid;
    private int galaxyMapId;
    private IntegerArray regionId = new IntegerArray(16);

}

package com.go2super.packet.map;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseMapBlockFightPacket extends Packet {

    public static final int TYPE = 1256;

    private int blockId;
    private short galaxyMapId;

    private short dataLen;
    private IntegerArray data = new IntegerArray(250);

}
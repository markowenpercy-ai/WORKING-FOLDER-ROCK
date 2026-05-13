package com.go2super.packet.map;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseConsortiaStarPacket extends Packet {

    public static final int TYPE = 1577;

    private short galaxyMapId;
    private short dataLen;

    private IntegerArray data = new IntegerArray(250);

}
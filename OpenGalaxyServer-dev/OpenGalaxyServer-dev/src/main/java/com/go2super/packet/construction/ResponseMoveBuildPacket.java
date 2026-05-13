package com.go2super.packet.construction;

import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseMoveBuildPacket extends Packet {

    public static final int TYPE = 1235;

    private int indexId;
    private UnsignedShort posX = new UnsignedShort(0);
    private UnsignedShort posY = new UnsignedShort(0);

}

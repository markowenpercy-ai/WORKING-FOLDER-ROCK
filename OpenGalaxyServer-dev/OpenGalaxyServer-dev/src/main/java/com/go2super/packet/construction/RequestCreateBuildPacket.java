package com.go2super.packet.construction;

import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCreateBuildPacket extends Packet {

    public static final int TYPE = 1200;

    private int seqId;
    private int guid;

    private int buildingId;
    private int indexId;
    private UnsignedShort posX = UnsignedShort.of(0);
    private UnsignedShort posY = UnsignedShort.of(0);

}

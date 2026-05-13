package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestConsortiaBuildingPacket extends Packet {

    public static final int TYPE = 1226;

    private int seqId;
    private int guid;

    private int galaxyMapId;
    private int galaxyId;

    private int indexId;

}

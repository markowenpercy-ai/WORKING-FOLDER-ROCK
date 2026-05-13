package com.go2super.packet.shipmodel;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestAddShipModelDelPacket extends Packet {

    public static final int TYPE = 1375;

    private int seqId;
    private int guid;

    private int shipModelId;

}

package com.go2super.packet.shipmodel;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCreateShipPacket extends Packet {

    public static final int TYPE = 1309;

    private int seqId;
    private int guid;

    private int shipModelId;
    private int num;
    private int type;

}

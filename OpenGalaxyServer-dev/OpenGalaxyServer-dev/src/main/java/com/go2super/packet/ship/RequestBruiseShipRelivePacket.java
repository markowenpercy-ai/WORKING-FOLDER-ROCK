package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestBruiseShipRelivePacket extends Packet {

    public static final int TYPE = 1422;

    private int seqId;
    private int guid;

    private int kind;
    private int shipModelId;
    private int num;

}

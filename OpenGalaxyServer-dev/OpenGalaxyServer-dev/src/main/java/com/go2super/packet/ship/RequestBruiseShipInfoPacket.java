package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestBruiseShipInfoPacket extends Packet {

    public static final int TYPE = 1418;

    private int seqId;
    private int guid;

}

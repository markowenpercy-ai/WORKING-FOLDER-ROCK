package com.go2super.packet.shipmodel;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCreateShipInfoPacket extends Packet {

    public static final int TYPE = 1313;

    private int seqId;
    private int guid;

}

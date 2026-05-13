package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCommanderInfoArrangePacket extends Packet {

    public static final int TYPE = 1515;

    private int seqId;
    private int guid;

}

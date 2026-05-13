package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCancelTechPacket extends Packet {

    public static final int TYPE = 1217;

    private int seqId;
    private int guid;

    private int techId;

}

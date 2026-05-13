package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCreateTechPacket extends Packet {

    public static final int TYPE = 1209;

    private int seqId;
    private int guid;

    private int techId;
    private int creditFlag;


}

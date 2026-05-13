package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestTechUpgradeInfoPacket extends Packet {

    public static final int TYPE = 1215;

    private int seqId;
    private int guid;

}

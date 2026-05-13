package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaUpgradePacket extends Packet {

    public static final int TYPE = 1583;

    private int seqId;
    private int guid;

    private int kind;

}

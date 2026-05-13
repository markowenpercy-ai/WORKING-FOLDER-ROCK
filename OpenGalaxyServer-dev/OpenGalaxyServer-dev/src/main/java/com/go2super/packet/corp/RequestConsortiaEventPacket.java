package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaEventPacket extends Packet {

    public static final int TYPE = 1598;

    private int seqId;
    private int guid;

    private int pageId;
    private int kind;

}

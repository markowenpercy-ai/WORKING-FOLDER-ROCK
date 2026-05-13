package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaAuthUserPacket extends Packet {

    public static final int TYPE = 1574;

    private int seqId;
    private int guid;

    private int pageId;

}

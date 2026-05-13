package com.go2super.packet.chat;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestLookupUserInfoPacket extends Packet {

    public static final int TYPE = 1625;

    private int seqId;
    private int guid;
    private int objGuid;
    private int objGalaxyId;

}

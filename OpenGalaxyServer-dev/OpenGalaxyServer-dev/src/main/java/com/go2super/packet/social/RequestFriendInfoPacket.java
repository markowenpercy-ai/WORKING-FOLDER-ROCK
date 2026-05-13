package com.go2super.packet.social;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFriendInfoPacket extends Packet {

    public static final int TYPE = 1615;

    private int seqId;
    private int guid;

    private int objGuid;
    private long objUserId;

}

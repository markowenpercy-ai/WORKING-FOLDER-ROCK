package com.go2super.packet.friend;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestAddFriendPacket extends Packet {

    public static final int TYPE = 1601;

    private int seqId;
    private int guid;

    private int objGuid;

}
package com.go2super.packet.friend;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFriendListPacket extends Packet {

    public static final int TYPE = 1605;

    private int seqId;
    private int guid;
    private int pageId;

    private int kind;
    private int online;

}
package com.go2super.packet.friend;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFriendPassAuthPacket extends Packet {

    public static final int TYPE = 1620;

    private int seqId;
    private int guid;

    private int friendGuid;

}
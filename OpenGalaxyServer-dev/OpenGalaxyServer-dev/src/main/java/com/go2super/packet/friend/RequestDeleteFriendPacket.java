package com.go2super.packet.friend;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDeleteFriendPacket extends Packet {

    public static final int TYPE = 1603;

    private int seqId;
    private int guid;

    private int friendGuid;

}
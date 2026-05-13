package com.go2super.packet.friend;

import com.go2super.obj.game.FriendInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFriendListPacket extends Packet {

    public static final int TYPE = 1606;

    private byte dataLen;
    private byte kind;

    private short friendCount;
    private List<FriendInfo> friendInfos;

}
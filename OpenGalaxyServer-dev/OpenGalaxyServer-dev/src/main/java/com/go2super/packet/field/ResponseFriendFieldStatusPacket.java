package com.go2super.packet.field;

import com.go2super.obj.game.FriendFieldStatus;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFriendFieldStatusPacket extends Packet {

    public static final int TYPE = 1815;

    private short kind;
    private short dataLen;

    private List<FriendFieldStatus> data = new ArrayList();

}
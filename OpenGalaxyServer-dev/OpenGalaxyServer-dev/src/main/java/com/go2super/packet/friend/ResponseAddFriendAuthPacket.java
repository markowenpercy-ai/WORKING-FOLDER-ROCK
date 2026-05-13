package com.go2super.packet.friend;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseAddFriendAuthPacket extends Packet {

    public static final int TYPE = 1619;

    private int srcGuId;
    private long srcUserId;

    private SmartString srcName = SmartString.of(32);

}
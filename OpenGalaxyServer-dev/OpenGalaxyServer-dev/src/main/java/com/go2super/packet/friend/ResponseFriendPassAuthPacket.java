package com.go2super.packet.friend;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseFriendPassAuthPacket extends Packet {

    public static final int TYPE = 1621;

    private int friendGuid;
    private long userId;

    private SmartString friendName = SmartString.of(32);

}
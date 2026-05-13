package com.go2super.packet.chat;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUserInfoPacket extends Packet {

    public static final int TYPE = 1623;

    private int seqId;

    private int guid;
    private int objGuid;
    private int objGalaxyId;
    private int reserve;

    private long userId;
    private SmartString userName = SmartString.of(32);

}

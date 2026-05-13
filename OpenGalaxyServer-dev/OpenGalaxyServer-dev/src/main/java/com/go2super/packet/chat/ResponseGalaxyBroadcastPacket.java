package com.go2super.packet.chat;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGalaxyBroadcastPacket extends Packet {

    public static final int TYPE = 1113;

    private int guid;
    private long userId;

    private SmartString name = SmartString.of(32);
    private int kind;

}

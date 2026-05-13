package com.go2super.packet.chat;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseLookupUserInfoPacket extends Packet {

    public static final int TYPE = 1626;

    private int guid;
    private long userId;
    private SmartString userName = SmartString.of(32);

    private int posX;
    private int posY;
    private int galaxyId;
    private int type;

}

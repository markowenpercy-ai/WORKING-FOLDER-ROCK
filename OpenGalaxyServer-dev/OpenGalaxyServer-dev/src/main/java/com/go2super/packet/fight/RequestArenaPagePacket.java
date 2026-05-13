package com.go2super.packet.fight;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestArenaPagePacket extends Packet {

    public static final int TYPE = 1452;

    private int seqId;
    private int guid;

    private UnsignedShort pageId = UnsignedShort.of(0);

    private UnsignedChar itemNum = UnsignedChar.of(0);
    private UnsignedChar arenaFlag = UnsignedChar.of(0);

    private SmartString cName = SmartString.of(32);

}

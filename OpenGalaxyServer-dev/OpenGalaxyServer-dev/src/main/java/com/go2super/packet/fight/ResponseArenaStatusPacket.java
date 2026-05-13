package com.go2super.packet.fight;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseArenaStatusPacket extends Packet {

    public static final int TYPE = 1451;

    private int guid;
    private SmartString cName = SmartString.of(32);

    private int roomId;
    private UnsignedChar request = UnsignedChar.of(0);

    // 0 = Room creation failed
    // 1 = Room exit
    private byte status;

}
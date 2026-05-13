package com.go2super.packet.corp;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCreateConsortiaPacket extends Packet {

    public static final int TYPE = 1554;

    private int seqId;
    private int guid;

    private SmartString name = SmartString.of(32);
    private SmartString proclaim = SmartString.of(256);

    private UnsignedChar headId = UnsignedChar.of(0);

}

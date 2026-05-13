package com.go2super.packet.fight;

import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseWarfieldStatusPacket extends Packet {

    public static final int TYPE = 1461;

    private int warfield;
    private UnsignedShort userNumber;
    private byte status;
    private byte matchLevel;

}
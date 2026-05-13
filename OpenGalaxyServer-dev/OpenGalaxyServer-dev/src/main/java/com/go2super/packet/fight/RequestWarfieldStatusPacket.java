package com.go2super.packet.fight;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestWarfieldStatusPacket extends Packet {

    public static final int TYPE = 1460;

    private int seqId;
    private int guid;
    private int findId;

    private UnsignedChar roomId = UnsignedChar.of(0);
    private UnsignedChar request = UnsignedChar.of(0);

}

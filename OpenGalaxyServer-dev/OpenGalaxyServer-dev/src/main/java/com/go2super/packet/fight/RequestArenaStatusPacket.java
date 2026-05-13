package com.go2super.packet.fight;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestArenaStatusPacket extends Packet {

    public static final int TYPE = 1450;

    private int seqId;
    private int guid;

    private int roomId;

    //
    //
    // 2 = Leave room
    private UnsignedChar request = UnsignedChar.of(0);

}

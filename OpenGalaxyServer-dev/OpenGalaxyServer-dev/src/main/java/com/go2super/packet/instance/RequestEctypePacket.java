package com.go2super.packet.instance;

import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestEctypePacket extends Packet {

    public static final int TYPE = 1134;

    private int seqId;
    private int guid;

    private short ectypeId;
    private UnsignedChar gateId = UnsignedChar.of(0);
    private UnsignedChar dataLen = UnsignedChar.of(0);

    private IntegerArray ships = new IntegerArray(60);

    private int passKey;
    private int roomId;

    private byte joinFlag;
    private byte activity;

    private UnsignedShort propsId = new UnsignedShort(0);
    private UnsignedChar capturePlace = new UnsignedChar(0);

    private byte reserve;

}

package com.go2super.packet.raids;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestCaptureStatePacket extends Packet {

    public static final int TYPE = 1457;

    private int seqId;
    private int guid;

    private UnsignedChar roomId = UnsignedChar.of(0);
    private UnsignedChar request = UnsignedChar.of(0);

}

package com.go2super.packet.igl;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseDuplicateStatusPacket extends Packet {

    public static final int TYPE = 1465;
    private int seqId;
    private int guid;
    private UnsignedChar duplicate;
    private UnsignedChar status;
}

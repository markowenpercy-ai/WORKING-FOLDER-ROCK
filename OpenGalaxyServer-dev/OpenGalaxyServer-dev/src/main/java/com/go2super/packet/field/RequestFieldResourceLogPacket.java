package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFieldResourceLogPacket extends Packet {

    public static final int TYPE = 1810;

    private int seqId;
    private int guid;

}

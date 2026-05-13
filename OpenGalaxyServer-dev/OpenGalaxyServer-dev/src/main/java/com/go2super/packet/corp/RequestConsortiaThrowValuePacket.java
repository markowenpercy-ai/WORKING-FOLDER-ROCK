package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaThrowValuePacket extends Packet {

    public static final int TYPE = 1562;

    private int seqId;
    private int guid;

    private int value;
    private int kind;

}

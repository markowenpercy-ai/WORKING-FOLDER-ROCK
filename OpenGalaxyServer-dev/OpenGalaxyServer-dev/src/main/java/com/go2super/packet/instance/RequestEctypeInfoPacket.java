package com.go2super.packet.instance;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestEctypeInfoPacket extends Packet {

    public static final int TYPE = 1437;

    private int seqId;
    private int guid;

    private short ectypeId;
    private short request;

}

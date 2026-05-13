package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestRefreshWallPacket extends Packet {

    public static final int TYPE = 1087;

    private int seqId;
    private int guid;

    private int kind; //<- type

}

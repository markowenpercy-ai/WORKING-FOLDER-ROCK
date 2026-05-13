package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaGivenPacket extends Packet {

    public static final int TYPE = 1572;

    private int seqId;
    private int guid;

    private int objGuid;


}

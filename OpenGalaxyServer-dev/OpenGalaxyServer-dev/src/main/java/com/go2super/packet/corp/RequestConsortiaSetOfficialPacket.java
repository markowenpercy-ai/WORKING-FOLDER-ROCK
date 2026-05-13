package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaSetOfficialPacket extends Packet {

    public static final int TYPE = 1570;

    private int seqId;
    private int guid;

    private int objGuid;

    private byte job;
    private byte kind;

}

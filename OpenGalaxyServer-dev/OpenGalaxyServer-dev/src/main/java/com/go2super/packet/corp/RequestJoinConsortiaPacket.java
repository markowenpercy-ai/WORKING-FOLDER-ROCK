package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestJoinConsortiaPacket extends Packet {

    public static final int TYPE = 1566;

    private int seqId;
    private int guid;

    private int consortiaId;

}

package com.go2super.packet.star;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestFromResourceStarToHomePacket extends Packet {

    public static final int TYPE = 1346;

    private int seqId;
    private int guid;

    private int galaxyMapId;
    private int galaxyId;
    private int shipTeamId;

}
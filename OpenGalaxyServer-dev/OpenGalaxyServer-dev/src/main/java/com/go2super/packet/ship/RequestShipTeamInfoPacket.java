package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestShipTeamInfoPacket extends Packet {

    public static final int TYPE = 1371;

    private int seqId;
    private int guid;

    private int galaxyMapId;
    private int galaxyId;
    private int shipTeamId;

}

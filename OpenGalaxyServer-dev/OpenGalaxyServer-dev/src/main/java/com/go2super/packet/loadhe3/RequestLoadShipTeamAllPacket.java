package com.go2super.packet.loadhe3;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestLoadShipTeamAllPacket extends Packet {

    public static final int TYPE = 1351;

    private int seqId;
    private int guid;

}

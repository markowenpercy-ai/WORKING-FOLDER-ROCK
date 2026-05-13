package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestDirectionShipTeamPacket extends Packet {

    public static final int TYPE = 1338;

    private int seqId;
    private int guid;

    private int shipTeamId;
    private byte direction;

}

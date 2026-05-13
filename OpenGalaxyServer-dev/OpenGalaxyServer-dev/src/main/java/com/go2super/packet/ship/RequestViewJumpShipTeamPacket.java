package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestViewJumpShipTeamPacket extends Packet {

    public static final int TYPE = 1407;

    private int seqId;
    private int guid;

    private int shipTeamId;
    private byte kind;

}
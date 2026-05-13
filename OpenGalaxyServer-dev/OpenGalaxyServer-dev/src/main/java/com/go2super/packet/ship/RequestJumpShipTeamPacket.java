package com.go2super.packet.ship;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestJumpShipTeamPacket extends Packet {

    public static final int TYPE = 1400;

    private int seqId;
    private int guid;

    private int toGalaxyMapId;
    private int toGalaxyId;

    private short dataLen;
    private byte jumpType;
    private byte kind;

    private IntegerArray shipTeamId = new IntegerArray(100);

}

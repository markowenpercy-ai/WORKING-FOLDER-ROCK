package com.go2super.packet.igl;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestSetRacingShipTeamPacket extends Packet {
    public static final int TYPE = 1860;

    public int seqId;
    private int guid;
    private int shipTeamLen;
    private IntegerArray ships = new IntegerArray(12);
}

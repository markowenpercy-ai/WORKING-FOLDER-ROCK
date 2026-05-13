package com.go2super.packet.igl;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class ResponseSetRacingShipTeamPacket extends Packet {

    public static final int TYPE = 1861;

    private int shipTeamLen;
    private IntegerArray ships = new IntegerArray(12);

}

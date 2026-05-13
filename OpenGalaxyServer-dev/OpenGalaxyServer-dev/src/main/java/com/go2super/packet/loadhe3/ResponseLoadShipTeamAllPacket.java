package com.go2super.packet.loadhe3;

import com.go2super.obj.game.ShipHe3Info;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseLoadShipTeamAllPacket extends Packet {

    public static final int TYPE = 1352;

    private int seqId;
    private int guid;

    private int dataLen;
    private List<ShipHe3Info> ships = new ArrayList<>();

}

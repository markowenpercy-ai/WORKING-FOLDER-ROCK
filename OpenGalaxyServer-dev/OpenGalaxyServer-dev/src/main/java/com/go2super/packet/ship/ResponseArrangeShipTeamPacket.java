package com.go2super.packet.ship;

import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ResponseArrangeShipTeamPacket extends Packet {

    public static final int TYPE = 1319;

    private short dataLen;
    private short kind;

    @Trash(length = 120)
    private List<ShipTeamNum> shipNums = new ArrayList<>();

}

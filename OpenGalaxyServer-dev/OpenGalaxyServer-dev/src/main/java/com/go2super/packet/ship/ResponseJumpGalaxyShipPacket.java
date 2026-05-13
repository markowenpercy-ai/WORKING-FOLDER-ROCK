package com.go2super.packet.ship;

import com.go2super.obj.game.JumpGalaxyShipInfo;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseJumpGalaxyShipPacket extends Packet {

    public static final int TYPE = 1358;

    private int galaxyId;

    private byte dataLen;
    private byte kind;

    private byte galaxyMapId;
    private byte jumpType; // 0 = Defend | 1 = Attack | 2 = Select | 3 = ¿Instance? | 4 = ¿?

    @Trash(length = 18)
    List<JumpGalaxyShipInfo> ships = new ArrayList<>();

}

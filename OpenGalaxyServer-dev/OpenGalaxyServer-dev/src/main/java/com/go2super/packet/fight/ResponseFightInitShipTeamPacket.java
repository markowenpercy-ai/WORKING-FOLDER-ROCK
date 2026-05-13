package com.go2super.packet.fight;

import com.go2super.obj.game.FightInitShipTeamInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFightInitShipTeamPacket extends Packet {

    public static final int TYPE = 1416;

    private int galaxyMapId;
    private int galaxyId;
    private int dataLen;

    private List<FightInitShipTeamInfo> fleets = new ArrayList<>();

}
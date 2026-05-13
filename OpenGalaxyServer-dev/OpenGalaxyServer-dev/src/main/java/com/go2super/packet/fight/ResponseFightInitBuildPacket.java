package com.go2super.packet.fight;

import com.go2super.obj.game.FightInitBuild;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFightInitBuildPacket extends Packet {

    public static final int TYPE = 1417;

    private int galaxyId;
    private short galaxyMapId;

    private short dataLen;
    private List<FightInitBuild> fightInitBuilds = new ArrayList<>();

}
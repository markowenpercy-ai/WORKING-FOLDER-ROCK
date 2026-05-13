package com.go2super.packet.ship;

import com.go2super.obj.game.ShipTeamBody;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUnionShipTeamPacket extends Packet {

    public static final int TYPE = 1334;

    private int seqId;
    private int guid;

    private int galaxyMapId;
    private int galaxyId;

    private int shipTeamId;
    private int shipTeamId2;

    private int shipTeamGas;
    private int shipTeamGas2;

    private ShipTeamBody teamBody = new ShipTeamBody();

    private ShipTeamBody teamBody2 = new ShipTeamBody();

}

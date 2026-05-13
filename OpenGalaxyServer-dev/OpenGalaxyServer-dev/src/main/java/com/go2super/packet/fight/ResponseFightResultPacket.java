package com.go2super.packet.fight;

import com.go2super.obj.game.FightRobResource;
import com.go2super.obj.game.FightTotalExp;
import com.go2super.obj.game.FightTotalKill;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseFightResultPacket extends Packet {

    public static final int TYPE = 1415;

    private short kind;
    private short galaxyMapId;

    private long topAssaultUserId;
    private int galaxyId;

    private SmartString topAssaultModelName = SmartString.of("", 32);
    private SmartString topAssaultCommander = SmartString.of("", 32);
    private SmartString topAssaultOwner = SmartString.of("", 32);

    private int topAssaultValue;
    private short topAssaultBodyId;

    // 1 = LOST
    // 2 = WIN
    // 3 = DRAW
    private short victory;

    private int attackShipNumber;
    private int attackLossNumber;
    private int defendShipNumber;
    private int defendLossNumber;

    @Trash(length = 10)
    private List<FightTotalKill> kill = new ArrayList<>();
    @Trash(length = 10)
    private List<FightTotalExp> exp = new ArrayList<>();
    @Trash(length = 10)
    private List<FightRobResource> prize = new ArrayList<>();

}
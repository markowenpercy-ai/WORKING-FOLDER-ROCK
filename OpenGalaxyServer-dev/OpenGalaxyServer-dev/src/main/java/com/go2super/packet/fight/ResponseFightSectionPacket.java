package com.go2super.packet.fight;

import com.go2super.obj.game.CharArray;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.game.ShipFight;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFightSectionPacket extends Packet {

    public static final int TYPE = 1410;

    private int galaxyId;
    private UnsignedShort needTime = UnsignedShort.of(0);

    private short boutId;

    private int sourceShipTeamId;
    private int toShipTeamId;

    private int sourceRepairSupply;

    private IntegerArray sourceRepairShield = new IntegerArray(9);
    private IntegerArray sourceRepairEndure = new IntegerArray(9);
    private IntegerArray targetRepairShield = new IntegerArray(9);
    private IntegerArray targetRepairEndure = new IntegerArray(9);

    private byte galaxyMapId;
    private UnsignedChar sourceSkill = UnsignedChar.of(0);
    private byte sourceDirection = (char) 4;
    private byte delFlag;
    private byte bothStatus;
    private byte repelStep;

    private CharArray sourceMovePath = new CharArray(16);
    private CharArray targetMatrixId = new CharArray(9, -1);

    private List<ShipFight> shipFights = new ArrayList<>();

}
package com.go2super.packet.commander;

import com.go2super.obj.game.ShortArray;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCommanderStoneInfoPacket extends Packet {

    public static final int TYPE = 1533;

    private SmartString userName = SmartString.of(32);
    private SmartString commanderZJ = SmartString.of("CCCCCCCC", 8);

    private int exp;
    private int skillId;
    private int level;
    private int cardLevel;

    private int aim;
    private int blench;
    private int priority;
    private int electron;

    private int stoneAim;
    private int stoneBlench;
    private int stoneElectron;
    private int stonePriority;

    private int stoneAssault;
    private int stoneEndure;
    private int stoneShield;
    private int stoneBlastHurt;
    private int stoneBlast;
    private int stoneDoubleHit;
    private int stoneRepairShield;
    private int stoneExp;

    private byte aimPer;
    private byte blenchPer;
    private byte priorityPer;
    private byte electronPer;

    private ShortArray cmos;

}

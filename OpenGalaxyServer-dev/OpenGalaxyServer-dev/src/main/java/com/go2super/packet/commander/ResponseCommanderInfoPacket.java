package com.go2super.packet.commander;

import com.go2super.obj.game.CommanderInfo;
import com.go2super.obj.game.IntegerArray;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.obj.game.ShortArray;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_NUM;

@Data
public class ResponseCommanderInfoPacket extends Packet {

    public static final int TYPE = 1507;

    private int commanderId;
    private int shipTeamId;
    private int restTime;
    private int exp;

    private short aim;
    private short blench;
    private short priority;
    private short electron;
    private short skill;
    private short cardLevel;

    private byte level;
    private byte cardType;
    private byte state;
    private byte showType;
    private SmartString commanderZJ = SmartString.of("CCCCCCCC", 8);

    @Trash(length = 9)
    private List<ShipTeamNum> teamBody = new ArrayList<>();

    private byte target;
    private byte targetInterval;
    private byte reserve;
    private byte allStatusLen;

    @Trash(length = MAX_COMMANDER_NUM)
    private List<CommanderInfo> allStatus = new ArrayList<>();

    private ShortArray stone = new ShortArray(12);

    private int stoneHole;
    private byte aimPer = (char) 32;
    private byte blenchPer = (char) 32;
    private byte priorityPer = (char) 32;
    private byte electronPer = (char) 32;

    private IntegerArray cmosExp = new IntegerArray(5);
    private ShortArray cmos = new ShortArray(5, -1);

}

package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseClearCommanderPercentPacket extends Packet {

    public static final int TYPE = 1513;

    private int commanderId;
    private int level;
    private int exp;

    private short aim;
    private short blench;
    private short priority;
    private short electron;

    private int lockFlag;

    private byte aimPer;
    private byte blenchPer;
    private byte priorityPer;
    private byte electronPer;

}

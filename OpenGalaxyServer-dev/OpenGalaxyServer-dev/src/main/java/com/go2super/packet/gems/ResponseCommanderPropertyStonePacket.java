package com.go2super.packet.gems;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseCommanderPropertyStonePacket extends Packet {

    public static final int TYPE = 1542;

    private int type;
    private int lockFlag;

    private int objStoneId;
    private int srcStoneId1;
    private int srcStoneId2;
    private int srcStoneId3;

    private int broFlag;
    private int guid;
    private SmartString name = SmartString.of(32);

}

package com.go2super.packet.gems;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCommanderPropertyStonePacket extends Packet {

    public static final int TYPE = 1541;

    private int seqId;
    private int guid;

    private int type;
    private int lockFlag;

    private int objStoneId;
    private int srcStoneId1;
    private int srcStoneId2;
    private int srcStoneId3;

}

package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUnionDoubleSkillCardPacket extends Packet {

    public static final int TYPE = 1535;

    private int seqId;
    private int guid;

    private int card1;
    private int card2;

    private int goods;
    private int goodsLockFlag;

    private int chip;
    private int chipLockFlag;

}

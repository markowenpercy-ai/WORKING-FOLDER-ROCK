package com.go2super.packet.upgrade;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestShipBodyUpgradePacket extends Packet {

    public static final int TYPE = 1355;

    private int seqId;
    private int guid;
    private int bodyPartId;

    private byte kind;
    private byte cancelFlag;

}

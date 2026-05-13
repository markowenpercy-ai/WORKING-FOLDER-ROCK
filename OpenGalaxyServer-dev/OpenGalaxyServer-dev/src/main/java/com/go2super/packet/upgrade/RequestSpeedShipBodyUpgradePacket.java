package com.go2super.packet.upgrade;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestSpeedShipBodyUpgradePacket extends Packet {

    public static final int TYPE = 1344;

    private int seqId;
    private int guid;
    private int bodyPartId;
    private int speedId;

    private byte kind;
    private byte feeType;

}

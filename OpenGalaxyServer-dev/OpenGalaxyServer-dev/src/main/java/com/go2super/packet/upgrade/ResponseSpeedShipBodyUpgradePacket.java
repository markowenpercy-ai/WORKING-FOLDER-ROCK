package com.go2super.packet.upgrade;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseSpeedShipBodyUpgradePacket extends Packet {

    public static final int TYPE = 1345;

    private int bodyPartId;
    private int spareTime;
    private int speedId;
    private int credit;

    private byte kind;
    private byte feeType;

}

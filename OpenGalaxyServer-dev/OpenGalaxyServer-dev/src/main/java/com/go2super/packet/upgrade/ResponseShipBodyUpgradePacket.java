package com.go2super.packet.upgrade;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseShipBodyUpgradePacket extends Packet {

    public static final int TYPE = 1356;

    private int bodyPartId;
    private int needTime;

    private byte kind;
    private byte cancelFlag;
    private short reserve;

    private int money;
    private int metal;
    private int gas;

}

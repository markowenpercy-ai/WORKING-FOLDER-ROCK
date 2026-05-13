package com.go2super.packet.upgrade;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseShipBodyUpgradeCompletePacket extends Packet {

    public static final int TYPE = 1370;

    private int bodyPartId;
    private byte kind;

}

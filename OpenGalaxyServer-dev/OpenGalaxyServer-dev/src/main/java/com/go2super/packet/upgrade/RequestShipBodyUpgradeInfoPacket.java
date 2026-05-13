package com.go2super.packet.upgrade;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestShipBodyUpgradeInfoPacket extends Packet {

    public static final int TYPE = 1368;

    private int seqId;
    private int guid;

}

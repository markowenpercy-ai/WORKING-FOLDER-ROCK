package com.go2super.packet.flagship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUpgradeFlagshipPacket extends Packet {

    public static final int TYPE = 1378;

    private int seqId;
    private int guid;
    private int shipBodyId;

}

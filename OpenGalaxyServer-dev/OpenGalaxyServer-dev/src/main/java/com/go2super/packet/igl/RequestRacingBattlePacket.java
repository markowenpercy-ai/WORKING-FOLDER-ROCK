package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestRacingBattlePacket extends Packet {
    public static final int TYPE = 1852;

    private int seqId;
    private long userId;
    private long objUserId;
    private int guid;
}

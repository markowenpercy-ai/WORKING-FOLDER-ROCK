package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestJoinRacingPacket extends Packet {
    public static final int TYPE = 1856;

    public int seqId;
    public long userId;
}

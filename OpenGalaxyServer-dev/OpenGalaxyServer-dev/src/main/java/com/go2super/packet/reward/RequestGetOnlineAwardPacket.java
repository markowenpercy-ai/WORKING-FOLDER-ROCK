package com.go2super.packet.reward;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestGetOnlineAwardPacket extends Packet {

    public static final int TYPE = 1093;

    private int seqId;
    private int guid;

}

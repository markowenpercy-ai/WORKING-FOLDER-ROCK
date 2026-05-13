package com.go2super.packet.task;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestTaskInfoPacket extends Packet {

    public static final int TYPE = 1064;

    private int seqId;
    private int guid;

}

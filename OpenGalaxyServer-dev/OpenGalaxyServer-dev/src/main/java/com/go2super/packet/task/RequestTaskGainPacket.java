package com.go2super.packet.task;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestTaskGainPacket extends Packet {

    public static final int TYPE = 1066;

    private int seqId;
    private int guid;
    private int taskId;
    private int kind;

}

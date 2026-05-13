package com.go2super.packet.task;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseTaskGainPacket extends Packet {

    public static final int TYPE = 1067;

    private int taskId;
    private int kind;
    private int nextTaskId;
    private int complete;
    private int gas;
    private int metal;
    private int money;
    private int propsId;
    private int propsNum;
    private int coins;

}

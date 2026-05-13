package com.go2super.packet.props;

import com.go2super.obj.game.TimeQueue;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseTimeQueuePacket extends Packet {

    public static final int TYPE = 1225;

    private int dataLen;
    private List<TimeQueue> timeQueues = new ArrayList<>();

}

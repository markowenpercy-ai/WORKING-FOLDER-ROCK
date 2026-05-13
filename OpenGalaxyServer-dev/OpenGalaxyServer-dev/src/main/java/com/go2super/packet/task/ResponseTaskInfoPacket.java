package com.go2super.packet.task;

import com.go2super.obj.game.ByteArray;
import com.go2super.obj.game.TaskInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseTaskInfoPacket extends Packet {

    public static final int TYPE = 1065;

    private short dataLen;
    private short awardLen;

    private ByteArray byteArray;
    private List<TaskInfo> taskInfos = new ArrayList<>();

}

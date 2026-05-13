package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGrowFieldResourcePacket extends Packet {

    public static final int TYPE = 1803;

    private int resourceId;
    private int errorCode; // 0 = No error, 1 = Conflicting territory

    private int needTime;
    private int galaxyId;
    private int num;

}

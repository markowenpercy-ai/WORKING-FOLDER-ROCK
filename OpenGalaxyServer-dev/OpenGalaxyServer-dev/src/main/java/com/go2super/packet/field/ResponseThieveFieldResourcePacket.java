package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseThieveFieldResourcePacket extends Packet {

    public static final int TYPE = 1807;

    private int errorCode;
    private int galaxyId;

    private int gas;
    private int metal;
    private int money;

}

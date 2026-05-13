package com.go2super.packet.recycle;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseDestroyShipPacket extends Packet {

    public static final int TYPE = 1365;

    private int gas;
    private int metal;
    private int money;

}
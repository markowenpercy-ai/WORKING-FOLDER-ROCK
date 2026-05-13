package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGetFieldResourcePacket extends Packet {

    public static final int TYPE = 1805;

    private int galaxyId;

    private int gas;
    private int metal;
    private int money;
    private int coins;

}

package com.go2super.packet.shipmodel;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseCreateShipPacket extends Packet {

    public static final int TYPE = 1310;

    private int shipModelId;
    private int needTime;

    private int num;

    private int gas;
    private int metal;
    private int money;

}
package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseBruiseShipRelivePacket extends Packet {
    public static final int TYPE = 1423;

    private int errorCode;
    private int type;
    private int shipModelId;
    private int num;
    private int needTime;
}

package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseSpeedBruiseShipPacket extends Packet {
    public static final int TYPE = 1421;

    private int errorCode;
    private int shipModelId;
    private int spareTime;
}

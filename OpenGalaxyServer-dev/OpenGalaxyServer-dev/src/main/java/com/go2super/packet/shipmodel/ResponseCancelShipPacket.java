package com.go2super.packet.shipmodel;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseCancelShipPacket extends Packet {

    public static final int TYPE = 1312;

    private int indexId;
    private int num;
    private int status;

}
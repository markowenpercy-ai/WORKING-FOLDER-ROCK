package com.go2super.packet.galaxy;

import com.go2super.packet.Packet;
import lombok.Data;


@Data
public class ResponseDeleteServerPacket extends Packet {

    public static final int TYPE = 1086;

    private int errorCode;

}

package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseAddPackPacket extends Packet {

    public static final int TYPE = 1058;

    private int propsPack;

}

package com.go2super.packet.flagship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUnionFlagshipPacket extends Packet {

    public static final int TYPE = 1377;

    private int propsId;

}

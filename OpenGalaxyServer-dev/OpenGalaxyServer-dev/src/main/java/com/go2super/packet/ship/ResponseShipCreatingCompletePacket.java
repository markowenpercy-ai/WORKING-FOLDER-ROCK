package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseShipCreatingCompletePacket extends Packet {

    public static final int TYPE = 1315;

    private int indexId;

}

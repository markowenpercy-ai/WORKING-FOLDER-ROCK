package com.go2super.packet.flagship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUpgradeShipPropsPacket extends Packet {

    public static final int TYPE = 1383;

    private int shipBodyId;

}

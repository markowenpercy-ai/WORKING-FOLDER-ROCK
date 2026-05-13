package com.go2super.packet.flagship;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUpgradeFlagshipPacket extends Packet {

    public static final int TYPE = 1379;

    private int shipBodyId;

}

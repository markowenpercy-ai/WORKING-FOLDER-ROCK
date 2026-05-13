package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseDeleteCommanderPacket extends Packet {

    public static final int TYPE = 1505;

    private int commanderId;


}

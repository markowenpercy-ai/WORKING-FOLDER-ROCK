package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseResumeCommanderPacket extends Packet {

    public static final int TYPE = 1511;

    private int commanderId;
    private int propsId;
    private int lockFlag;

}

package com.go2super.packet.commander;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseReliveCommanderPacket extends Packet {

    public static final int TYPE = 1509;

    private int commanderId;
    private int propsId;
    private int lockFlag;

}

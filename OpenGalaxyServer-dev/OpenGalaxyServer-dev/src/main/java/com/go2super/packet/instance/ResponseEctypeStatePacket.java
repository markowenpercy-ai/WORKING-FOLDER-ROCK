package com.go2super.packet.instance;

import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseEctypeStatePacket extends Packet {

    public static final int TYPE = 1135;

    private short ectypeId;

    // What is this?
    private UnsignedChar gateId;

    // States:
    // 0 - Finish
    // 1 - Start Instance
    // 2 - Match in Progress
    // 3 - ???
    // 4 - ???
    private byte state;

}
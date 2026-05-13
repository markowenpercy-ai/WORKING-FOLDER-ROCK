package com.go2super.packet.station;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUpdatePlayerNamePacket extends Packet {

    public static final int TYPE = 1024;

    private int seqId;
    private int guid;

    private SmartString name = SmartString.of(32);

}

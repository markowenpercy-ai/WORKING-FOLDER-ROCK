package com.go2super.packet.loadhe3;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestLoadShipTeamPacket extends Packet {

    public static final int TYPE = 1328;

    private int seqId;
    private int guid;

    private int shipTeamId;
    private UnsignedInteger gas = UnsignedInteger.of(0);

}

package com.go2super.packet.loadhe3;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestUnloadShipTeamPacket extends Packet {

    public static final int TYPE = 1332;

    private int seqId;
    private int guid;

    private int shipTeamId;
    private UnsignedInteger gas = UnsignedInteger.of(0);

}

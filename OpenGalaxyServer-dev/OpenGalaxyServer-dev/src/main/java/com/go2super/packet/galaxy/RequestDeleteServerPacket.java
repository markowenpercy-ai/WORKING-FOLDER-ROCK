package com.go2super.packet.galaxy;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDeleteServerPacket extends Packet {

    public static final int TYPE = 1085;

    public int seqId;
    public int guid;

    public long userId;

}

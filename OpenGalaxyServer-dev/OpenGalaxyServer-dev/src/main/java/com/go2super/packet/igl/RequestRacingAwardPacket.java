package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestRacingAwardPacket extends Packet {

    public static final int TYPE = 1854;

    public int seqId;
    public int guid;

}

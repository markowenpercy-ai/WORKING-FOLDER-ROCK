package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestRacingInformationPacket extends Packet {

    public static final int TYPE = 1850;

    public int seqId;

    public long userId;
    public int guid;

}

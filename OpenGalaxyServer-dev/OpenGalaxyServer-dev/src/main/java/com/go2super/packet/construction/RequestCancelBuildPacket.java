package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCancelBuildPacket extends Packet {

    public static final int TYPE = 1205;

    private int seqId;
    private int guid;

    private int indexId;

}

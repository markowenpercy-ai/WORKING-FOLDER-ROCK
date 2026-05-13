package com.go2super.packet.props;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestMovePropPacket extends Packet {

    public static final int TYPE = 1052;

    private int seqId;
    private int guid;

    private int kind;
    private int propId;
    private int propNum;
    private int lockFlag;

}

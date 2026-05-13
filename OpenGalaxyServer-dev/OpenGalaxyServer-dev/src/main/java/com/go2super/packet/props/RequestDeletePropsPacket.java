package com.go2super.packet.props;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDeletePropsPacket extends Packet {

    public static final int TYPE = 1059;

    private int seqId;
    private int guid;

    private int propsId;
    private byte lockFlag;

}

package com.go2super.packet.props;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestUsePropsPacket extends Packet {

    public static final int TYPE = 1055;

    private int seqId;
    private int guid;
    private int propsId;
    private int lockFlag;
    private int num;

}

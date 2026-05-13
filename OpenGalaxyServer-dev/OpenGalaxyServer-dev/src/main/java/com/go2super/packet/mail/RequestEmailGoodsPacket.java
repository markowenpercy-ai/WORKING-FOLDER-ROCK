package com.go2super.packet.mail;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestEmailGoodsPacket extends Packet {

    public static final int TYPE = 1613;

    private int seqId;
    private int guid;

    private int autoId;
    private int propsId;

}

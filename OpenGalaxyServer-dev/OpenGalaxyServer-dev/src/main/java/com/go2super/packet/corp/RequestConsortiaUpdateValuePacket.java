package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaUpdateValuePacket extends Packet {

    public static final int TYPE = 1591;

    private int seqId;
    private int guid;

    private int needUnionValue;
    private int needShopValue;


}

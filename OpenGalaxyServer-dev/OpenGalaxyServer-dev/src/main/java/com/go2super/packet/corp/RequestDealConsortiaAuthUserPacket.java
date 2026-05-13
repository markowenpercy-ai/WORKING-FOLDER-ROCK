package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestDealConsortiaAuthUserPacket extends Packet {

    public static final int TYPE = 1576;

    private int seqId;
    private int guid;

    private int objGuid;
    private int agree;

}

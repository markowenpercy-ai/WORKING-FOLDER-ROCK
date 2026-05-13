package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaUpgradeCancelPacket extends Packet {

    public static final int TYPE = 1596;

    private int seqId;
    private int guid;

    private int kind;


}

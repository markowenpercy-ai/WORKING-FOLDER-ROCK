package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestHelpFieldCenterResourcePacket extends Packet {

    public static final int TYPE = 1812;

    private int seqId;
    private int guid;

    private int objGuid;

}

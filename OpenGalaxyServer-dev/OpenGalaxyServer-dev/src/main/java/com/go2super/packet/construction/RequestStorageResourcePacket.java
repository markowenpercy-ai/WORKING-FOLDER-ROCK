package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestStorageResourcePacket extends Packet {

    public static final int TYPE = 1236;

    private int seqId;
    private int guid;

}

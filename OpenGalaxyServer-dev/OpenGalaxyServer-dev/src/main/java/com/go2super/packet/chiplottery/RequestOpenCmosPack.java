package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestOpenCmosPack extends Packet {

    public static final int TYPE = 1906;

    private int seqId;
    private int guid;

}

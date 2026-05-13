package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaThrowRankPacket extends Packet {

    public static final int TYPE = 1578;

    private int seqId;
    private int guid;

    private int pageId;
    private int firstOpenFlag;

}

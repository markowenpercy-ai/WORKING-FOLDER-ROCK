package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.Data;


@Data
public class RequestInsertFlagConsortiaMemberPacket extends Packet {

    public static final int TYPE = 1650;

    private int seqId;
    private int guid;

    private int consortiaId;
    private int pageId;

}

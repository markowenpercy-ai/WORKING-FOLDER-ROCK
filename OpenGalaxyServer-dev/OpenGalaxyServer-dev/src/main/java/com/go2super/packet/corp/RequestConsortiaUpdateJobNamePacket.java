package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaJobName;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestConsortiaUpdateJobNamePacket extends Packet {

    public static final int TYPE = 1590;

    private int seqId;
    private int guid;

    private ConsortiaJobName consortiaJobName = new ConsortiaJobName("", "", "", "", "");

}

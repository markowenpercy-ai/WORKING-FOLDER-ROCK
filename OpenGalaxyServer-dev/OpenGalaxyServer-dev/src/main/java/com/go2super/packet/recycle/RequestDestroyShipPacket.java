package com.go2super.packet.recycle;

import com.go2super.obj.game.ShipDestroyInfo;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestDestroyShipPacket extends Packet {

    public static final int TYPE = 1364;

    private int seqId;
    private int guid;
    private int dataLen;

    private ShipDestroyInfo shipNums = new ShipDestroyInfo();

}
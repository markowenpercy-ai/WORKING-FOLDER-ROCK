package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestMoveShipTeamPacket extends Packet {

    public static final int TYPE = 1322;

    private int seqId;
    private int guid;

    private int shipTeamId;
    private int posX;
    private int posY;

}

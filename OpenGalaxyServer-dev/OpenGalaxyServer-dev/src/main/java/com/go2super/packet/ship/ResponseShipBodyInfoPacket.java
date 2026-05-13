package com.go2super.packet.ship;

import com.go2super.obj.game.ShortArray;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseShipBodyInfoPacket extends Packet {

    public static final int TYPE = 1308;

    private short bodyNum;
    private short partNum;

    private ShortArray bodyId;
    private ShortArray partId;

}

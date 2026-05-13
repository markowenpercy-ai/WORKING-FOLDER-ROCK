package com.go2super.packet.shipmodel;

import com.go2super.obj.game.ShortArray;
import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCreateShipModelPacket extends Packet {

    public static final int TYPE = 1300;

    private int seqId;
    private int guid;

    private SmartString shipName = SmartString.of(32);

    private short bodyId;
    private short partNum;

    private ShortArray parts = new ShortArray(50);

}

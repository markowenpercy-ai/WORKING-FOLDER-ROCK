package com.go2super.packet.fight;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseDuplicateBulletinPacket extends Packet {
    public static final int TYPE = 1462;

    private byte duplicateType;
    private byte bulletinType;
    private short countdown;

    /*
    [System]:The Galaxy Championships will begin in 0 minutes. Players that want to join the tournament must register now!
     */
}

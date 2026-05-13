package com.go2super.packet.custom;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class CustomMoreInfoPacket extends Packet {

    public static final int TYPE = 5001;

    private long userId;
    private int guid;

    private byte kind;
    private int icon;

}

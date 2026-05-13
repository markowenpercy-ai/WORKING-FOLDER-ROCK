package com.go2super.packet.custom;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class CustomWarScorePacket extends Packet {

    public static final int TYPE = 5003;

    private int points;

}

package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;


@Data
public class ResponseRacingBattlePacket extends Packet {

    public static final int TYPE = 1853;

    private int errorCode;
    private long userId;

}

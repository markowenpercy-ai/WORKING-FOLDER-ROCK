package com.go2super.packet.raids;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseJumpShipTeamNoticePacket extends Packet {

    public static final int TYPE = 1445;

    // 0 = My planet
    // 1 = Corp members
    // 2 = My planet and corp members
    private int kind;

}

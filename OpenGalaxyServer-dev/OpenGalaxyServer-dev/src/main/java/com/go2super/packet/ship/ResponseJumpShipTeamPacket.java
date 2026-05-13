package com.go2super.packet.ship;

import com.go2super.obj.game.JumpShipTeamInfo;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseJumpShipTeamPacket extends Packet {

    public static final int TYPE = 1401;

    private JumpShipTeamInfo data;

}

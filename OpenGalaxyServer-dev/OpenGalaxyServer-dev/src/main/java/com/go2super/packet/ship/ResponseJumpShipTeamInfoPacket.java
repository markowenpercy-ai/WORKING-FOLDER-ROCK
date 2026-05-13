package com.go2super.packet.ship;

import com.go2super.obj.game.JumpShipTeamInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseJumpShipTeamInfoPacket extends Packet {

    public static final int TYPE = 1403;

    private int dataLen;
    private List<JumpShipTeamInfo> transmission = new ArrayList<>();

}

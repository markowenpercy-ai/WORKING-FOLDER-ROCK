package com.go2super.packet.igl;

import com.go2super.obj.game.RacingShipTeamInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;


@Data
public class ResponseRacingShipTeamInfoPacket extends Packet {

    public static final int TYPE = 1865;

    private byte dataLen;
    private byte kind;

    private List<RacingShipTeamInfo> shipTeamInfos = new ArrayList<>();

}

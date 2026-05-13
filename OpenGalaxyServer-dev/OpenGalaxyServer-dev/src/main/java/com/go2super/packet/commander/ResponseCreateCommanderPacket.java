package com.go2super.packet.commander;

import com.go2super.obj.game.CommanderBaseInfo;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCreateCommanderPacket extends Packet {

    public static final int TYPE = 1501;

    private int nextInviteTime;
    private CommanderBaseInfo commanderBaseInfo;

}

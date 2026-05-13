package com.go2super.packet.commander;

import com.go2super.obj.game.CommanderBaseInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseCommanderBaseInfoPacket extends Packet {

    public static final int TYPE = 1514;

    private int dataLen;
    private int nextInviteTime;
    private int reserve;

    private List<CommanderBaseInfo> commanderBaseInfos = new ArrayList<>();

}

package com.go2super.packet.commander;

import com.go2super.obj.game.RefreshCommanderBaseInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseRefreshCommanderBaseInfoPacket extends Packet {

    public static final int TYPE = 1527;

    private int dataLen;

    private List<RefreshCommanderBaseInfo> commanderBaseInfos = new ArrayList<>();

}

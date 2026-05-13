package com.go2super.packet.rank;

import com.go2super.obj.game.RankFightInfo;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ResponseRankFightPacket extends Packet {

    public static final int TYPE = 1705;

    private int pageId;
    private int maxPageId;

    private int dataLen;
    private List<RankFightInfo> fights = new ArrayList<>();

}

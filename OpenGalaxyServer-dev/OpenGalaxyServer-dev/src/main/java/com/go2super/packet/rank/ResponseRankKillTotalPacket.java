package com.go2super.packet.rank;

import com.go2super.obj.game.RankUserInfo;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ResponseRankKillTotalPacket extends Packet {

    public static final int TYPE = 1703;

    private int pageId;
    private int maxPageId;

    private int dataLen;
    private List<RankUserInfo> users = new ArrayList<>();

}

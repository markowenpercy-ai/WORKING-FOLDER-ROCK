package com.go2super.packet.league;

import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ResponseLeagueMatchPacket extends Packet {
    public static final int TYPE = 1707;
    private int pageId;
    private int maxPageId;
    private int dataLen;
    private List<RankMatch> data = new ArrayList<>();
}

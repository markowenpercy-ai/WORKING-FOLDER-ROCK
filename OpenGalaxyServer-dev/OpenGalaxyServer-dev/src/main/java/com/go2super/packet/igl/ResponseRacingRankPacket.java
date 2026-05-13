package com.go2super.packet.igl;

import com.go2super.packet.Packet;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;


@Data
public class ResponseRacingRankPacket extends Packet {

    public static final int TYPE = 1868;

    private int userCount;
    private long userId;
    private int pageId;
    private int dataLen;
    private List<RacingRank> rankList = new ArrayList<>();

}

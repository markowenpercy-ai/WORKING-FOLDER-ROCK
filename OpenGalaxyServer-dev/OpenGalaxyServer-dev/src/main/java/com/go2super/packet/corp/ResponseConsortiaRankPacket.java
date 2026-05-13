package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaRank;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaRankPacket extends Packet {

    public static final int TYPE = 1586;

    private int consortiaCount;

    private int dataLen;

    private List<ConsortiaRank> data;

}

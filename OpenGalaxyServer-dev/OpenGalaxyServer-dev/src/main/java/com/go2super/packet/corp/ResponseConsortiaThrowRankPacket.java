package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaThrowRank;
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
public class ResponseConsortiaThrowRankPacket extends Packet {

    public static final int TYPE = 1579;


    private int throwValue;
    private int myWealth;

    private short memberCount;
    private short dataLen;

    private List<ConsortiaThrowRank> data;


}

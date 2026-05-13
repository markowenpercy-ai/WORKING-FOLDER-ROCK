package com.go2super.packet.mall;

import com.go2super.obj.game.TradeInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseMyTradeInfoPacket extends Packet {

    public static final int TYPE = 1753;

    private int dataLen;

    private List<TradeInfo> trades;

}
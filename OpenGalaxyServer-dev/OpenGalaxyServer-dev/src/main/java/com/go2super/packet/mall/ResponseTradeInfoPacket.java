package com.go2super.packet.mall;

import com.go2super.obj.game.TradeInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseTradeInfoPacket extends Packet {

    public static final int TYPE = 1757;

    private int tradeCount;
    private int dataLen;
    private int reserve;

    private List<TradeInfo> trades;

}
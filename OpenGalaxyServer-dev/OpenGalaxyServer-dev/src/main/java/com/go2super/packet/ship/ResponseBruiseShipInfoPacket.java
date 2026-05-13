package com.go2super.packet.ship;

import com.go2super.obj.game.BruiseShipInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseBruiseShipInfoPacket extends Packet {

    public static final int TYPE = 1419;

    private int dataLen;
    private int shipModelId;
    private int num;
    private int needTime;

    private List<BruiseShipInfo> deadShipData = new ArrayList<>();

}

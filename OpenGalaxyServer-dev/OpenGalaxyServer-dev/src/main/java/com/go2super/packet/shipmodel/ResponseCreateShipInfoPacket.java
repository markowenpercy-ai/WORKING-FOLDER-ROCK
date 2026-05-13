package com.go2super.packet.shipmodel;

import com.go2super.obj.game.CreateShipInfo;
import com.go2super.obj.utility.Trash;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseCreateShipInfoPacket extends Packet {

    public static final int TYPE = 1314;

    private int maxCreateShipNum;

    private short incShipPercent;
    private short dataLen;

    @Trash(length = 10)
    List<CreateShipInfo> createShipList = new ArrayList<>();

}

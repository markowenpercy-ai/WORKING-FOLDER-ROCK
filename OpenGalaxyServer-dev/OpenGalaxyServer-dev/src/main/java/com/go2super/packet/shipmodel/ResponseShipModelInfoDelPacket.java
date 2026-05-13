package com.go2super.packet.shipmodel;

import com.go2super.obj.game.ShipModelInfo;
import com.go2super.obj.utility.Trash;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseShipModelInfoDelPacket extends Packet {

    public static final int TYPE = 1304;

    private UnsignedShort dataLen; // 3
    @Trash(length = 3)
    private List<ShipModelInfo> shipModelInfoList = new ArrayList<>();

}

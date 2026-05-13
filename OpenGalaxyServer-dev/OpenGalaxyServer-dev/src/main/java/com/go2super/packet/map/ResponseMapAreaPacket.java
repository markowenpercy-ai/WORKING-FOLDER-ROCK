package com.go2super.packet.map;

import com.go2super.obj.game.MapArea;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseMapAreaPacket extends Packet {

    public static final int TYPE = 1251;

    private UnsignedShort regionId;
    private byte galaxyMapId;

    private UnsignedChar dataLen;
    private List<MapArea> mapAreaList = new ArrayList<>();

}
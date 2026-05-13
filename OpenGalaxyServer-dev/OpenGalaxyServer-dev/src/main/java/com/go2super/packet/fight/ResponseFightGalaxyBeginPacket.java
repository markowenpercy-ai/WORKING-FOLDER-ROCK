package com.go2super.packet.fight;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseFightGalaxyBeginPacket extends Packet {

    public static final int TYPE = 1133;

    private int galaxyMapId;
    private int galaxyId;

    private int kind;

    private int pirateLevelId;
    private int consortiaId;
    private SmartString consortiaName = SmartString.of("", 32);

}
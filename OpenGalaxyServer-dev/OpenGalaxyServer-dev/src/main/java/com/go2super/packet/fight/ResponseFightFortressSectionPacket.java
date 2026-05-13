package com.go2super.packet.fight;

import com.go2super.obj.game.FortressFight;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFightFortressSectionPacket extends Packet {

    public static final int TYPE = 1444;

    private int galaxyId;
    private int sourceId;

    private short galaxyMapId;
    private short boutId;

    // 1 = Particle Cannon
    // 2 = Anti Aircraft Cannon
    // 3 = Thor / Space Station
    private byte buildType;

    private UnsignedChar reserve1 = UnsignedChar.of(0);
    private UnsignedChar reserve2 = UnsignedChar.of(0);

    private byte dataLen;
    private List<FortressFight> fortressFights = new ArrayList<>();

}
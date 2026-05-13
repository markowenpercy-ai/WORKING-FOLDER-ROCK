package com.go2super.packet.fight;

import com.go2super.obj.game.ArenaPageInfo;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.UnsignedShort;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseArenaPagePacket extends Packet {

    public static final int TYPE = 1453;

    private UnsignedShort pageId = UnsignedShort.of(0);
    private UnsignedShort pageNum = UnsignedShort.of(0);

    private UnsignedChar arenaFlag = UnsignedChar.of(0);
    private UnsignedChar dataLen = UnsignedChar.of(0);

    private List<ArenaPageInfo> arenas = new ArrayList<>();

}
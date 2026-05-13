package com.go2super.packet.chat;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUserInfoPacket extends Packet {

    public static final int TYPE = 1624;

    private int guid;
    private long userId;

    private SmartString userName = SmartString.of(32);
    private SmartString consortia = SmartString.of(32);

    private int rankId;
    private int posX;
    private int posY;
    private int peaceTime;
    private int galaxyId;

    private byte spaceLevel;
    private byte cityLevel;

    private UnsignedChar levelId;
    private UnsignedChar matchLevel;

    private int passMaxEctype;
    private int consortiaId;

    private int passInsertFlagTime;
    private int insertFlagConsortiaId;
    private SmartString insertFlagConsortia = SmartString.of(32);

}

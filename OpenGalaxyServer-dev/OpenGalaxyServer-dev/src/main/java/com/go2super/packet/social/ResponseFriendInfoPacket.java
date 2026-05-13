package com.go2super.packet.social;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseFriendInfoPacket extends Packet {

    public static final int TYPE = 1616;

    private int guid;
    private long userId;

    private int galaxyMapId;
    private int galaxyId;

    private int exp;
    private int levelId;

    private int fightFlag;
    private int starType;

}

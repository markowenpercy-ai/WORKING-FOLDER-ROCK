package com.go2super.packet.chat;

import com.go2super.obj.utility.WideString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ChatMessagePacket extends Packet {

    public static final int TYPE = 1600;

    private int seqId;
    private long srcUserId;
    private long objUserId;
    private int guid;
    private int objGuid;
    private short channelType;
    private short specialType;
    private int propsId;
    private WideString corpAcronym = WideString.of(64);
    private int acronym;
    private int rank;
    private WideString name = WideString.of(32);
    private WideString toName = WideString.of(32);
    private WideString buffer = WideString.of(128);

    @Override
    public int getCustomSize() {

        return 570; // Unused it calculates 424 but i think that is really util have callback for future custom sizes
    }

}

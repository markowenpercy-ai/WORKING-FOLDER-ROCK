package com.go2super.packet.custom;

import com.go2super.obj.utility.WideString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class CustomWarnPacket extends Packet {

    public static final int TYPE = 5000;

    private int seqId;
    private long srcUserId;
    private long objUserId;
    private int guid;
    private int objGuid;
    private short channelType;
    private short specialType;
    private int propsId;
    private WideString name = WideString.of(32);
    private WideString toName = WideString.of(32);
    private WideString buffer = WideString.of(1024);

    @Override
    public int getCustomSize() {

        return 1330; // Unused it calculates 424 but i think that is really util have callback for future custom sizes
    }

}

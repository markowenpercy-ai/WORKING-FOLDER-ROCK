package com.go2super.packet.match;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseMatchInfoPacket extends Packet {

    public static final int TYPE = 1447;

    private int spareTime;
    private int matchWeekTop;
    private short reserve;

    private byte matchWin;
    private byte matchLost;
    private byte matchDogfall;
    private byte matchLevel;
    private byte matchCount;
    private byte matchType;

}

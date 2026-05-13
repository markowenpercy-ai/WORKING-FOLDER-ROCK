package com.go2super.packet.commander;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseUnionCommanderCardBroPacket extends Packet {

    public static final int TYPE = 1526;

    private int guid;
    private long userId;

    private SmartString name = SmartString.of(32);

    private int skillId;
    private int cardLevel;
    private int successFlag;

}

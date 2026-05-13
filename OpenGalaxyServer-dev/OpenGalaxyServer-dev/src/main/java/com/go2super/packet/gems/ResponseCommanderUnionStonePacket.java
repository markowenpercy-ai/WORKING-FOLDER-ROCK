package com.go2super.packet.gems;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseCommanderUnionStonePacket extends Packet {

    public static final int TYPE = 1531;

    private int propsId;
    private int lockFlag;

    private int broFlag;
    private int guid;
    private SmartString name = SmartString.of(32);

}

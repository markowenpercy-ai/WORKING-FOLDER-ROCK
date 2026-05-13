package com.go2super.packet.gems;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseCommanderInsertStonePacket extends Packet {

    public static final int TYPE = 1529;

    private int gemType;
    private int commanderId;
    private int holeId;
    private int propsId;
    private int lockFlag;

}

package com.go2super.packet.gems;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCommanderUnionStonePacket extends Packet {

    public static final int TYPE = 1530;

    private int seqId;
    private int guid;

    private int propsId;
    private int lockFlag;

}

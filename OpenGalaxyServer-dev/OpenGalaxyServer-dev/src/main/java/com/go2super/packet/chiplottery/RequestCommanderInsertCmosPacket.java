package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestCommanderInsertCmosPacket extends Packet {

    public static final int TYPE = 1539;

    private int seqId;
    private int guid;

    private int cmosType;
    private int commanderId;
    private int holeId;
    private int cmosId;

}

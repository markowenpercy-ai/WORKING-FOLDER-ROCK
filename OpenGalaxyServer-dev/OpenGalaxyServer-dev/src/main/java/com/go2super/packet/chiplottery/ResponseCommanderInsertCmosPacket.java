package com.go2super.packet.chiplottery;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCommanderInsertCmosPacket extends Packet {

    public static final int TYPE = 1540;

    private int kind;
    private int commanderId;
    private int holeId;
    private int cmosId;

}

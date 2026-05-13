package com.go2super.packet.corp;

import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class RequestEditConsortiaPacket extends Packet {

    public static final int TYPE = 1560;

    private int seqId;
    private int guid;

    private SmartString notice = SmartString.of(VariableType.MAX_MEMO);
    private SmartString proclaim = SmartString.of(VariableType.MAX_MEMO);

    private byte headId;

}

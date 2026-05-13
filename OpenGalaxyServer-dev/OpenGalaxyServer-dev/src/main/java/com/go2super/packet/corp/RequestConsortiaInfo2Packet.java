package com.go2super.packet.corp;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestConsortiaInfo2Packet extends Packet {

    public static final int TYPE = 1657;

    private int seqId;
    private int guid;

    private SmartString name = SmartString.of(32);

}

package com.go2super.packet.mail;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RequestSendEmailPacket extends Packet {

    public static final int TYPE = 1607;

    private int seqId;
    private int guid;

    private int sendGuid;
    private SmartString subject = SmartString.of(32);
    private SmartString content = SmartString.of(512);

}

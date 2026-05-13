package com.go2super.packet.login;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class PlayerLoginTogPacket extends Packet {

    public static final int TYPE = 503;
    public int size;

    private int seqId;
    private long userId;
    private SmartString sessionKey = SmartString.of(128);
    private int serverId;
    private int flag;
    private SmartString registerName = SmartString.of(32);

}

package com.go2super.packet.login;

import com.go2super.obj.utility.SmartString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class PlayerLoginServerValidatePacket extends Packet {

    public static final int TYPE = 504;

    private int port;
    private long userId;

    private SmartString ip = SmartString.of(32);
    private SmartString sessionKey = SmartString.of(25);


}

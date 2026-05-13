package com.go2super.packet.custom;

import com.go2super.obj.utility.WideString;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class CustomConfigurationPacket extends Packet {

    public static final int TYPE = 5002;

    private WideString resourcesUrl = WideString.of(256);

}

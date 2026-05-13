package com.go2super.packet.field;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseHelpFieldCenterResourcePacket extends Packet {

    public static final int TYPE = 1813;

    private int errorCode;
    private int objGuid;

}

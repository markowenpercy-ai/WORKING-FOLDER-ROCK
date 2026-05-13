package com.go2super.packet.construction;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResponseConsortiaWealthPacket extends Packet {

    public static final int TYPE = 1582;

    private UnsignedInteger wealth = UnsignedInteger.of(0);

}

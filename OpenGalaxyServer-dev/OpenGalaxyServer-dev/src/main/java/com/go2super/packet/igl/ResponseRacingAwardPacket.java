package com.go2super.packet.igl;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;


@Data
public class ResponseRacingAwardPacket extends Packet {

    public static final int TYPE = 1855;

    private UnsignedInteger amount = UnsignedInteger.of(0);

}

package com.go2super.packet.raids;

import com.go2super.obj.utility.UnsignedInteger;
import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseCaptureArkRoom extends Packet {
    public static final int TYPE = 1456;

    private UnsignedInteger rightPropsID = UnsignedInteger.of(0);
    private UnsignedInteger leftPropsID = UnsignedInteger.of(0);
    private UnsignedInteger countDown = UnsignedInteger.of(0);

    private int roomState = 0;
    private int roomID = 0;
}

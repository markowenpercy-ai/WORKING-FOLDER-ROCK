package com.go2super.packet.raids;

import com.go2super.obj.game.CaptureArk;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseCaptureArkListPacket extends Packet {

    public static final int TYPE = 1455;

    private UnsignedChar searchFleets = UnsignedChar.of(0);
    private UnsignedChar captureFleets = UnsignedChar.of(0);

    private UnsignedChar reserve = UnsignedChar.of(0);
    private UnsignedChar dataLen = UnsignedChar.of(0);

    private List<CaptureArk> rooms = new ArrayList<>();

}

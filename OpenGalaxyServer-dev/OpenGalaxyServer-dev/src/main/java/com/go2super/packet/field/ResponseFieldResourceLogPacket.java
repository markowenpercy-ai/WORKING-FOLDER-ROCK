package com.go2super.packet.field;

import com.go2super.obj.game.FieldResourceLog;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseFieldResourceLogPacket extends Packet {

    public static final int TYPE = 1811;

    private int dataLen;

    private List<FieldResourceLog> data = new ArrayList<>();

}

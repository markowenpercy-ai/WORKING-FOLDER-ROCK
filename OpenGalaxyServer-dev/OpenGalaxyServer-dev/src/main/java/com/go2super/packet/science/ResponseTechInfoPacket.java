package com.go2super.packet.science;

import com.go2super.obj.game.TechInfo;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponseTechInfoPacket extends Packet {

    public static final int TYPE = 1212;

    private int dataLen;
    private List<TechInfo> techInfoList = new ArrayList<>();

}

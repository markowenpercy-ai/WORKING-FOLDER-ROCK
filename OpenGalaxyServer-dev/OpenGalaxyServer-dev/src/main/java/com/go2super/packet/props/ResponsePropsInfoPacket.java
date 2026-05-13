package com.go2super.packet.props;

import com.go2super.obj.game.Prop;
import com.go2super.packet.Packet;
import lombok.Data;

import java.util.*;

@Data
public class ResponsePropsInfoPacket extends Packet {

    public static final int TYPE = 1054;

    private int dataLen;
    private List<Prop> propList = new ArrayList<>();

}

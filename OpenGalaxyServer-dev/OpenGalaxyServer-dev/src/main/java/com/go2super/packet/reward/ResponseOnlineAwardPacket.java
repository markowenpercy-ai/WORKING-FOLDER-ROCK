package com.go2super.packet.reward;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseOnlineAwardPacket extends Packet {

    public static final int TYPE = 1092;

    private int propsId;
    private int propsNum;
    private int spareTime;

}

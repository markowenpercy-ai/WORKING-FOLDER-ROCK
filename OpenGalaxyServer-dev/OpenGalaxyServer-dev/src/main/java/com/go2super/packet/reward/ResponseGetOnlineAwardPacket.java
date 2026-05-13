package com.go2super.packet.reward;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseGetOnlineAwardPacket extends Packet {

    public static final int TYPE = 1094;

    private int errorCode;

    private int propsId;
    private int propsNum;

}

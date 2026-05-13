package com.go2super.packet.friend;

import com.go2super.packet.Packet;
import lombok.Data;

@Data
public class ResponseAddFriendPacket extends Packet {

    public static final int TYPE = 1602;

    private int errorCode;

}
package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaEvent;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaEventPacket extends Packet {

    public static final int TYPE = 1599;

    private short eventCount;

    private byte kind;
    private byte dataLen;

    private List<ConsortiaEvent> data;

}

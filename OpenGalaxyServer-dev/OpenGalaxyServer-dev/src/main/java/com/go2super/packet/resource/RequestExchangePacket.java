package com.go2super.packet.resource;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestExchangePacket extends Packet {

    public static final int TYPE = 1089;

    private int seqId;
    private int guid;

    private int kind; //<---- type;
    private int value;

}

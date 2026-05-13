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
public class ResponseExchangePacket extends Packet {

    public static final int TYPE = 1090;

    private int metal;
    private int gas;

}

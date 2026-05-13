package com.go2super.packet.corp;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseConsortiaUpdateValuePacket extends Packet {

    public static final int TYPE = 1592;

    private int needUnionValue;
    private int needShopValue;

}

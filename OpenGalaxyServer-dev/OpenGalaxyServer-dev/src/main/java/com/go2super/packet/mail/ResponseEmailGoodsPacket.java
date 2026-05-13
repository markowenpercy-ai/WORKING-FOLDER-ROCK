package com.go2super.packet.mail;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseEmailGoodsPacket extends Packet {

    public static final int TYPE = 1614;

    private int autoId;
    private int propsId;

}

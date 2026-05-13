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
public class ResponseConsortiaBuyGoodsPacket extends Packet {

    public static final int TYPE = 1594;

    private int errorCode;
    private int goodsId;
    private int price;

}

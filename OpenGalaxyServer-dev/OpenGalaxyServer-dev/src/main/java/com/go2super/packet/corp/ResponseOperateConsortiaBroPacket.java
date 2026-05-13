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
public class ResponseOperateConsortiaBroPacket extends Packet {

    public static final int TYPE = 1568;

    private int consortiaId;
    private int type;  // 1 leave, 0 join, 2 changed
    private int propsCorpsPack;

    private byte job;
    private byte unionLevel;
    private byte shopLevel;
    private byte reserve2;

    private int needUnionValue;
    private int needShopValue;


}

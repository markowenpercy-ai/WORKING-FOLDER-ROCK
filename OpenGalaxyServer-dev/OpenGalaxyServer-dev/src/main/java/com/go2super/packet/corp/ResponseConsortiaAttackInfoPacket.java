package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaAttackInfo;
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
public class ResponseConsortiaAttackInfoPacket extends Packet {

    public static final int TYPE = 1589;

    private short attackCount;
    private short dataLen;

    private List<ConsortiaAttackInfo> data;

}

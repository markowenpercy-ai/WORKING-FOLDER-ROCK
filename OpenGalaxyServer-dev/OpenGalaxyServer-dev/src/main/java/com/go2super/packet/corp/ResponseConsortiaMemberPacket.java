package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaMember;
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
public class ResponseConsortiaMemberPacket extends Packet {

    public static final int TYPE = 1559;

    private int dataLen;
    private List<ConsortiaMember> data;

}

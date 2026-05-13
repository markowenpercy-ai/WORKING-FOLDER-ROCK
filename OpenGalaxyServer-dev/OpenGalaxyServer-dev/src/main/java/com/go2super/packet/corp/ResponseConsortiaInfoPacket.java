package com.go2super.packet.corp;

import com.go2super.obj.game.ConsortiaInfo;
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
public class ResponseConsortiaInfoPacket extends Packet {

    public static final int TYPE = 1551;

    private int consortiaCount;
    private short dataLen;

    private short pageId;

    private List<ConsortiaInfo> data = new ArrayList<>();

}

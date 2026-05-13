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
public class RequestConsortiaInfoPacket extends Packet {

    public static final int TYPE = 1550;

    private int seqId;
    private int guid;

    private int pageId;

}

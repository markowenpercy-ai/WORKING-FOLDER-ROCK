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
public class ResponseCreateConsortiaPacket extends Packet {

    public static final int TYPE = 1555;

    private int ConsortiaId;
    private int propsCorpsPack;

    private byte errorCode;
    private byte lockFlag;

}

package com.go2super.packet.science;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCreateTechCompletePacket extends Packet {

    public static final int TYPE = 1219;

    private int techId;

}

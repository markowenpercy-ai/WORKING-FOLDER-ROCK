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
public class ResponseCreateTechPacket extends Packet {

    public static final int TYPE = 1210;

    private int techId;
    private int needTime;
    private int creditFlag;

}

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
public class ResponseSpeedTechPacket extends Packet {

    public static final int TYPE = 1229;

    private int techId;
    private int techSpeedId;

    private int time;
    private int credit;

}

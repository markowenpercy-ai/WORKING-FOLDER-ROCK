package com.go2super.packet.construction;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseSpeedBuildPacket extends Packet {

    public static final int TYPE = 1223;

    private int indexId;
    private int buildingSpeedId;
    private int time;
    private int credit;

}

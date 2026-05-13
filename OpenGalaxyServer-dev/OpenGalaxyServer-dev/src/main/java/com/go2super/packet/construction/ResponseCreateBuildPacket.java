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
public class ResponseCreateBuildPacket extends Packet {

    public static final int TYPE = 1201;

    private int buildingId;

    private int levelId;
    private int indexId;

    private int needTime;

    private int he3;
    private int metal;
    private int money;

}

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
public class ResponseCancelBuildPacket extends Packet {

    public static final int TYPE = 1206;

    private int indexId;

    private int gas;
    private int metal;
    private int money;

    private byte status;

}

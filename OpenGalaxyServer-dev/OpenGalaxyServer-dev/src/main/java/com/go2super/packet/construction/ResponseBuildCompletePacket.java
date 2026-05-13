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
public class ResponseBuildCompletePacket extends Packet {

    public static final int TYPE = 1204;

    private int galaxyMapId;
    private int galaxyId;
    private int indexId;

}

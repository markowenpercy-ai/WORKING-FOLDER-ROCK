package com.go2super.packet.planet;

import com.go2super.obj.game.MapArea;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseMoveHomeBroPacket extends Packet {

    public static final int TYPE = 1104;

    private int delGalaxyId;
    private MapArea mapArea;

}

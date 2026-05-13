package com.go2super.packet.ship;

import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCancelJumpShipTeamPacket extends Packet {

    public static final int TYPE = 1439;

    private int shipTeamId;
    private int needTime;

}

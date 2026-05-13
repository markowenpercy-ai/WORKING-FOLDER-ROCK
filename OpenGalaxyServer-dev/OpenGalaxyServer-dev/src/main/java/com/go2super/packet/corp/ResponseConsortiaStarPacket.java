package com.go2super.packet.corp;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseConsortiaStarPacket extends Packet {

    public static final int TYPE = 1577;

    private short galaxyMapId;
    private short dataLen;

    private IntegerArray data = new IntegerArray(250);

}

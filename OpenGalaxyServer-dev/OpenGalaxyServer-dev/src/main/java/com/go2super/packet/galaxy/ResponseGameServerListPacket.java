package com.go2super.packet.galaxy;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseGameServerListPacket extends Packet {

    public static final int TYPE = 1072;

    private int guid;
    private int dataLen;

    IntegerArray data = new IntegerArray(60);
    IntegerArray registerNumber = new IntegerArray(60);
    IntegerArray onlineNumber = new IntegerArray(60);

}

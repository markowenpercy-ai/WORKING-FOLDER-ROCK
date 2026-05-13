package com.go2super.packet.ship;

import com.go2super.obj.game.IntegerArray;
import com.go2super.packet.Packet;
import lombok.Data;

import static com.go2super.obj.utility.VariableType.MAX_COMMANDER_NUM;

@Data
public class ResponseCommanderInfoArrangePacket extends Packet {

    public static final int TYPE = 1516;

    private int dataLen;
    private IntegerArray data = new IntegerArray(MAX_COMMANDER_NUM);

}

package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class FortressFight extends BufferObject {

    public int targetShipTeamId;
    public int targetReduceSupply;
    public int targetReduceStorage;

    public int[] targetReduceHp = new int[9];
    public int[] targetReduceShipNum = new int[9];

    public byte delFlag;
    private byte reserve;

    public FortressFight() {

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(targetShipTeamId);
        go2buffer.addUnsignInt(targetReduceSupply);
        go2buffer.addUnsignInt(targetReduceStorage);

        for (int value : targetReduceHp) {
            go2buffer.addInt(value);
        }

        for (int value : targetReduceShipNum) {
            go2buffer.addUnsignShort(value);
        }

        go2buffer.addByte(delFlag);
        go2buffer.addByte(reserve);

    }

    @Override
    public FortressFight trash() {

        return new FortressFight();
    }

}
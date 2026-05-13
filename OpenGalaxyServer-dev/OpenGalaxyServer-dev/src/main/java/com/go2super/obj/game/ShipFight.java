package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class ShipFight extends BufferObject {

    private int sourceReduceSupply;
    private int targetReduceSupply;

    private int sourceReduceStorage;
    private int targetReduceStorage;

    private int sourceReduceHp;
    private IntegerArray targetReduceShield = new IntegerArray(9, -1);

    private int targetReduceEndure;
    private int sourceReduceShipNum;

    private IntegerArray targetReduceShipNum = new IntegerArray(9, -1);

    private IntegerArray sourcePartId = new IntegerArray(7, -1);
    private IntegerArray sourcePartNum = new IntegerArray(7, -1);
    private IntegerArray sourcePartRate = new IntegerArray(7, -1);

    private IntegerArray targetPartId = new IntegerArray(7, -1);
    private IntegerArray targetPartNum = new IntegerArray(7, -1);

    private int sourceSkill;
    private int targetSkill;
    private int targetBlast;

    public ShipFight() {

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(sourceReduceSupply);
        go2buffer.addInt(targetReduceSupply);
        go2buffer.addInt(sourceReduceStorage);
        go2buffer.addInt(targetReduceStorage);
        go2buffer.addInt(sourceReduceHp);

        for (int value : targetReduceShield.getArray()) {
            go2buffer.addInt(value);
        }

        go2buffer.addInt(targetReduceEndure);
        go2buffer.addUnsignShort(sourceReduceShipNum);

        for (int value : targetReduceShipNum.getArray()) {
            go2buffer.addUnsignShort(value);
        }

        for (int value : sourcePartId.getArray()) {
            go2buffer.addShort(value);
        }

        for (int value : sourcePartNum.getArray()) {
            go2buffer.addUnsignChar(value);
        }

        for (int value : sourcePartRate.getArray()) {
            go2buffer.addUnsignChar(value);
        }

        for (int value : targetPartId.getArray()) {
            go2buffer.addShort(value);
        }

        for (int value : targetPartNum.getArray()) {
            go2buffer.addUnsignChar(value);
        }

        go2buffer.addUnsignChar(sourceSkill);
        go2buffer.addUnsignChar(targetSkill);

        go2buffer.addChar(targetBlast);

    }

    @Override
    public ShipFight trash() {

        return new ShipFight();
    }

}
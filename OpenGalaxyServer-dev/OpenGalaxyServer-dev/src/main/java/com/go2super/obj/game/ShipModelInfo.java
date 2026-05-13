package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.socket.util.MathUtil;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
public class ShipModelInfo extends BufferObject {

    private SmartString shipName = SmartString.of(32);

    private char partNum;
    private char pubFlag;

    private short bodyId;
    private short[] partId = new short[50];

    private int shipModelId;

    public ShipModelInfo(String shipName, char partNum, char pubFlag, short bodyId, short[] partId, int shipModelId) {

        this.shipName.value(shipName);

        this.partNum = partNum;
        this.pubFlag = pubFlag;
        this.bodyId = bodyId;
        this.partId = partId;
        this.shipModelId = shipModelId;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addString(shipName.getValue(), shipName.getSize());
        go2buffer.addChar(partNum);
        go2buffer.addChar(pubFlag);
        go2buffer.addShort(bodyId);

        for (int i = 0; i < 50; i++) {
            go2buffer.addShort(partId.length > i ? partId[i] : 0);
        }

        go2buffer.addInt(shipModelId);

    }

    @Override
    public ShipModelInfo trash() {

        return of("", 0, 0, 0, new int[50], 0);
    }

    public static ShipModelInfo of(String name, int partNum, int pubFlag, int bodyId, int[] partId, int shipModelId) {

        if (partId.length != 50) {
            throw new IllegalArgumentException("PartId array need to have a length of 50!");
        }
        return new ShipModelInfo(name, (char) partNum, (char) pubFlag, (short) bodyId, MathUtil.toShortArray(partId), shipModelId);
    }

}

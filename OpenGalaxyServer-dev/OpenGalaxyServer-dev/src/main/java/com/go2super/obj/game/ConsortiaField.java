package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class ConsortiaField extends BufferObject {

    private int maxShipNum;
    private int shipNum;
    private int galaxyId;
    private int needTime;

    private byte status;
    private byte level;

    private short reserve;

    public ConsortiaField(int maxShipNum, int shipNum, int galaxyId, int needTime, byte status, byte level, short reserve) {

        this.maxShipNum = maxShipNum;
        this.shipNum = shipNum;
        this.galaxyId = galaxyId;
        this.needTime = needTime;

        this.status = status;
        this.level = level;

        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((4 - go2Buffer.getBuffer().position() % 4) % 4);

        go2Buffer.addInt(maxShipNum);
        go2Buffer.addInt(shipNum);
        go2Buffer.addInt(galaxyId);
        go2Buffer.addInt(needTime);

        go2Buffer.addByte(status);
        go2Buffer.addByte(level);

        go2Buffer.addShort(reserve);

    }

    @Override
    public ConsortiaField trash() {

        return new ConsortiaField(-1, -1, -1, -1, (byte) -1, (byte) -1, (short) -1);
    }

}

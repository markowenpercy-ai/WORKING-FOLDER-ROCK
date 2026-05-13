package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TechUpgradeInfo extends BufferObject {

    public int needTime;
    public short techId;
    public short creditFlag;

    public TechUpgradeInfo(int needTime, int techId, int creditFlag) {

        this.needTime = needTime;
        this.techId = (short) techId;
        this.creditFlag = (short) creditFlag;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(needTime);

        go2buffer.addShort(techId);
        go2buffer.addShort(creditFlag);

    }

    @Override
    public void read(Go2Buffer go2Buffer) {

        go2Buffer.getByte((4 - go2Buffer.getBuffer().position() % 4) % 4);

        needTime = go2Buffer.getInt();
        techId = (short) go2Buffer.getShort();
        creditFlag = (short) go2Buffer.getShort();

    }
}

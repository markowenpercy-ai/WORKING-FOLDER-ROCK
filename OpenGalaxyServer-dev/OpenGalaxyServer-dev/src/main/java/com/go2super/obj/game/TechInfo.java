package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class TechInfo extends BufferObject {

    public short techId;
    public short levelId;

    public TechInfo(int techId, int levelId) {

        this.techId = (short) techId;
        this.levelId = (short) levelId;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addShort(techId);
        go2buffer.addShort(levelId);

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        go2buffer.getByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        techId = (short) go2buffer.getShort();
        levelId = (short) go2buffer.getShort();

    }

}

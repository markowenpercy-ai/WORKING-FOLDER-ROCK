package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class ConsortiaInfo extends BufferObject {

    private String name;

    private int consortiaId;
    private int rankId;

    public ConsortiaInfo(String name, int consortiaId, int rankId) {

        this.name = name;
        this.consortiaId = consortiaId;
        this.rankId = rankId;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addString(name, 32);
        go2buffer.addInt(consortiaId);
        go2buffer.addInt(rankId);

    }

    @Override
    public ConsortiaInfo trash() {

        return new ConsortiaInfo("", 0, 0);
    }

}

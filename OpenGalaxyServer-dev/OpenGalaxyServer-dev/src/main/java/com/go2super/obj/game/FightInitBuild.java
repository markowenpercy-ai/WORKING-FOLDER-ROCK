package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class FightInitBuild extends BufferObject {

    private int maxEndure;
    private int endure;

    private int indexId;

    private char headId;
    private char reserve;

    public FightInitBuild(int maxEndure, int endure, int indexId, char headId, char reserve) {

        this.maxEndure = maxEndure;
        this.endure = endure;

        this.indexId = indexId;
        this.headId = headId;
        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((4 - go2Buffer.getBuffer().position() % 4) % 4);

        go2Buffer.addUnsignInt(maxEndure);
        go2Buffer.addUnsignInt(endure);

        go2Buffer.addUnsignShort(indexId);

        go2Buffer.addUnsignChar(headId);
        go2Buffer.addUnsignChar(reserve);

    }

    @Override
    public FightInitBuild trash() {

        return new FightInitBuild(0, 0, 0, (char) 0, (char) 0);
    }

}

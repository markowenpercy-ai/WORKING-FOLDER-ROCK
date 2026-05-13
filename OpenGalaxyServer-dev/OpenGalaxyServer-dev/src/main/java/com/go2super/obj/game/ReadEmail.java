package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class ReadEmail extends BufferObject {

    private int id;
    private int num;

    private short lockNum;
    private short bodyId;

    public ReadEmail(int id, int num, short lockNum, short bodyId) {

        this.id = id;
        this.num = num;

        this.lockNum = lockNum;
        this.bodyId = bodyId;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((4 - go2Buffer.getBuffer().position() % 4) % 4);

        go2Buffer.addInt(id);
        go2Buffer.addInt(num);

        go2Buffer.addShort(lockNum);
        go2Buffer.addShort(bodyId);

    }

    @Override
    public ReadEmail trash() {

        return new ReadEmail(-1, -1, (short) -1, (short) -1);
    }

}

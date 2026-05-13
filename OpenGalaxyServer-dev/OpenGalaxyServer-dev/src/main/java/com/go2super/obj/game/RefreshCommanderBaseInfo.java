package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class RefreshCommanderBaseInfo extends BufferObject {

    private int commanderId;
    private int exp;

    private int aim;
    private int blench;
    private int priority;
    private int electron;
    private int reserve;
    private int level;
    private int reserve2;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(commanderId);
        go2buffer.addInt(exp);

        go2buffer.addShort(aim);
        go2buffer.addShort(blench);
        go2buffer.addShort(priority);
        go2buffer.addShort(electron);
        go2buffer.addShort(reserve);

        go2buffer.addChar(level);
        go2buffer.addChar(reserve2);

    }

    @Override
    public RefreshCommanderBaseInfo trash() {

        return new RefreshCommanderBaseInfo();
    }

}

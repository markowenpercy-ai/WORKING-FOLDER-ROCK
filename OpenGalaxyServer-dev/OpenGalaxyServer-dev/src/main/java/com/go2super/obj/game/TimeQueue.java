package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TimeQueue extends BufferObject {

    public int type;
    public int spareTime;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(type);
        go2buffer.addInt(spareTime);

    }

    public static TimeQueue generate() {

        return new TimeQueue(0, 0);
    }

}

package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class EctypeInfo extends BufferObject {

    public int instance;

    public EctypeInfo(int instance) {

        this.instance = instance;
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(instance);
    }

}

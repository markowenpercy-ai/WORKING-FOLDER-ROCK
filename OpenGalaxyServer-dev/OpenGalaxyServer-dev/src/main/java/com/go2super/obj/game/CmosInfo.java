package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class CmosInfo extends BufferObject {

    private int exp;

    private short propsId;
    private short reserve;

    public CmosInfo(int exp, short propsId, short reserve) {

        this.exp = exp;

        this.propsId = propsId;
        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addUnsignInt(exp);
        go2buffer.addShort(propsId);
        go2buffer.addShort(reserve);

    }

    @Override
    public CmosInfo trash() {

        return new CmosInfo(0, (short) 0, (short) 0);
    }

}

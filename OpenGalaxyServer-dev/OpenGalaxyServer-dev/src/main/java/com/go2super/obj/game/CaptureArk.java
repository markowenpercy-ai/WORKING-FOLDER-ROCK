package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class CaptureArk extends BufferObject {

    private int rightPropsId;
    private int leftPropsId;
    private int countdown;

    private int roomState;
    private int roomId;

    public CaptureArk(int rightPropsId, int leftPropsId, int countdown, int roomState, int roomId) {

        this.rightPropsId = rightPropsId;
        this.leftPropsId = leftPropsId;

        this.countdown = countdown;
        this.roomState = roomState;

        this.roomId = roomId;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addUnsignShort(rightPropsId);
        go2buffer.addUnsignShort(leftPropsId);
        go2buffer.addUnsignShort(countdown);

        go2buffer.addUnsignChar(roomState);
        go2buffer.addUnsignChar(roomId);

    }

    @Override
    public CaptureArk trash() {

        return new CaptureArk(0, 0, 0, 0, 0);
    }

}

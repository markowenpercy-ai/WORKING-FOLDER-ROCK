package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class FieldResource extends BufferObject {

    private int spareTime;
    private int guid;
    private int galaxyId;
    private int num;
    private int resourceId;
    private int status;
    private int thieveCount;
    private int thieveFlag;

    public FieldResource(int spareTime, int guid, int galaxyId, int num, int resourceId, int status, int thieveCount, int thieveFlag) {

        this.spareTime = spareTime;
        this.guid = guid;
        this.galaxyId = galaxyId;
        this.num = num;
        this.resourceId = resourceId;
        this.status = status;
        this.thieveCount = thieveCount;
        this.thieveFlag = thieveFlag;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(spareTime);
        go2buffer.addInt(guid);
        go2buffer.addInt(galaxyId);
        go2buffer.addInt(num);

        go2buffer.addChar(resourceId);
        go2buffer.addChar(status);
        go2buffer.addChar(thieveCount);
        go2buffer.addChar(thieveFlag);

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        go2buffer.getByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        spareTime = go2buffer.getInt();
        guid = go2buffer.getInt();
        galaxyId = go2buffer.getInt();
        num = go2buffer.getInt();

        resourceId = go2buffer.getChar();
        status = go2buffer.getChar();
        thieveCount = go2buffer.getChar();
        thieveFlag = go2buffer.getChar();

    }

    @Override
    public FieldResource trash() {

        return new FieldResource(0, 0, 0, 0, 0, 0, 0, 0);
    }

}

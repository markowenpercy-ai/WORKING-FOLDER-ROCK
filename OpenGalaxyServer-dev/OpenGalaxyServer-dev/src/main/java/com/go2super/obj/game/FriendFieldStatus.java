package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class FriendFieldStatus extends BufferObject {

    private long userId;

    private int guid;
    private int galaxyMapId;
    private int galaxyId;

    private short reserve;

    private char helpFlag;
    private char thieveFlag;

    public FriendFieldStatus(long userId, int guid, int galaxyMapId, int galaxyId, short reserve, char helpFlag, char thieveFlag) {

        this.userId = userId;

        this.guid = guid;
        this.galaxyMapId = galaxyMapId;
        this.galaxyId = galaxyId;

        this.reserve = reserve;

        this.helpFlag = helpFlag;
        this.thieveFlag = thieveFlag;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(userId);

        go2buffer.addInt(guid);
        go2buffer.addInt(galaxyMapId);
        go2buffer.addInt(galaxyId);

        go2buffer.addShort(reserve);

        go2buffer.addChar(helpFlag);
        go2buffer.addChar(thieveFlag);

    }

    @Override
    public FriendFieldStatus trash() {

        return new FriendFieldStatus(0, 0, 0, 0, (short) 0, (char) 0, (char) 0);

    }
}

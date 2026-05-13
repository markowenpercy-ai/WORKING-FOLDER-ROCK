package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FriendInfo extends BufferObject {

    private SmartString name = SmartString.of(32);

    private long userId;

    private int guid;
    private int status;

    private int level;
    private int headId;

    private int reserve;

    public FriendInfo(String name, long userId, int guid, int status, int level, int headId, int reserve) {

        this.name.value(name);

        this.userId = userId;
        this.guid = guid;

        this.status = status;
        this.level = level;
        this.headId = headId;

        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addString(name.getValue(), name.getSize());

        go2buffer.addLong(userId);
        go2buffer.addInt(guid);
        go2buffer.addChar(status);
        go2buffer.addUnsignChar(level);
        go2buffer.addUnsignChar(headId);
        go2buffer.addChar(reserve);

    }

    @Override
    public FriendInfo trash() {

        return new FriendInfo("", -1, -1, 0, 0, 0, 0);
    }

}
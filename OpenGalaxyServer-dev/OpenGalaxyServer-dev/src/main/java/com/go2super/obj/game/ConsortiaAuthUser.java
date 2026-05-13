package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsortiaAuthUser extends BufferObject {

    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private long userId;

    private int killTotal;
    private int guid;
    private int level;
    private int reserve;

    public ConsortiaAuthUser(String name, long userId, int killTotal, int guid, int level, int reserve) {

        this.name.value(name);

        this.userId = userId;

        this.killTotal = killTotal;
        this.guid = guid;
        this.level = level;
        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);
        go2buffer.addString(name.noSpaces(), 32);

        go2buffer.addLong(userId);

        go2buffer.addInt(killTotal);
        go2buffer.addInt(guid);
        go2buffer.addInt(level);
        go2buffer.addInt(reserve);


    }

    @Override
    public ConsortiaAuthUser trash() {

        return new ConsortiaAuthUser("", -1, -1, -1, -1, -1);
    }
}

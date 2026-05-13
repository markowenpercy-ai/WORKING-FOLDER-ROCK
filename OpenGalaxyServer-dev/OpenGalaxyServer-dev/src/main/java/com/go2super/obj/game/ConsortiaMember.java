package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.UnsignedChar;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsortiaMember extends BufferObject {

    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private long userId;

    private int offlineTime;
    private int throwValue;
    private int guid;

    private UnsignedChar level = UnsignedChar.of(0);

    private char status;
    private char job;
    private char reserve;

    public ConsortiaMember(String name, long userId, int offlineTime, int throwValue, int guid, char level, char status, char job, char reserve) {

        this.name.value(name);

        this.userId = userId;

        this.offlineTime = offlineTime;
        this.throwValue = throwValue;
        this.guid = guid;

        this.level.setValue(level);

        this.status = status;
        this.job = job;
        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addString(name.noSpaces(), 32);

        go2buffer.addLong(userId);

        go2buffer.addInt(offlineTime);
        go2buffer.addInt(throwValue);
        go2buffer.addInt(guid);

        go2buffer.addUnsignChar(level.getValue());

        go2buffer.addChar(status);
        go2buffer.addChar(job);
        go2buffer.addChar(reserve);

    }

    @Override
    public ConsortiaMember trash() {

        return new ConsortiaMember("", -1, -1, -1, -1, (char) -1, (char) -1, (char) -1, (char) -1);
    }

}

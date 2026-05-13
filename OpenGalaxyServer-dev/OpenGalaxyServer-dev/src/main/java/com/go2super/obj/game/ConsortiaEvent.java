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
public class ConsortiaEvent extends BufferObject {

    private SmartString scrName = SmartString.of(VariableType.MAX_NAME);
    private SmartString objName = SmartString.of(VariableType.MAX_NAME);

    private long srcUserId;
    private long objUserId;

    private int guid;

    private int extend;
    private int passTime;

    private char bigType;
    private char smallType;

    private char jumpType;
    private char reserve;

    public ConsortiaEvent(String scrName, String objName, long srcUserId, long objUserId, int guid, int extend, int passTime, char bigType, char smallType, char jumpType, char reserve) {

        this.scrName.setValue(scrName);
        this.objName.setValue(objName);

        this.srcUserId = srcUserId;
        this.objUserId = objUserId;

        this.guid = guid;

        this.extend = extend;
        this.passTime = passTime;

        this.bigType = bigType;
        this.smallType = smallType;

        this.jumpType = jumpType;
        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((8 - go2Buffer.getBuffer().position() % 8) % 8);

        go2Buffer.addString(scrName.noSpaces(), VariableType.MAX_NAME);
        go2Buffer.addString(objName.noSpaces(), VariableType.MAX_NAME);

        go2Buffer.addLong(srcUserId);
        go2Buffer.addLong(objUserId);

        go2Buffer.addInt(guid);

        go2Buffer.addInt(extend);
        go2Buffer.addInt(passTime);

        go2Buffer.addChar(bigType);
        go2Buffer.addChar(smallType);

        go2Buffer.addChar(jumpType);
        go2Buffer.addChar(reserve);

    }

    @Override
    public ConsortiaEvent trash() {

        return new ConsortiaEvent("", "", -1, -1, -1, -1, -1, (char) -1, (char) -1, (char) -1, (char) -1);
    }

}

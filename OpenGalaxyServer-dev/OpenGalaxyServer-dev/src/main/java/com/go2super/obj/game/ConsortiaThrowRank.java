package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import com.go2super.obj.utility.VariableType;
import lombok.Data;

@Data
public class ConsortiaThrowRank extends BufferObject {

    private SmartString name = SmartString.of(VariableType.MAX_NAME);

    private long userId;

    private int throwRest;
    private int throwCredit;

    private int guid;
    private int rankId;

    public ConsortiaThrowRank(String name, long userId, int throwRest, int throwCredit, int guid, int rankId) {

        this.name.value(name);

        this.userId = userId;

        this.throwRest = throwRest;
        this.throwCredit = throwCredit;

        this.guid = guid;
        this.rankId = rankId;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addString(name.noSpaces(), VariableType.MAX_NAME);

        go2buffer.addLong(userId);

        go2buffer.addUnsignInt(throwRest);
        go2buffer.addUnsignInt(throwCredit);

        go2buffer.addInt(guid);
        go2buffer.addInt(rankId);


    }

    @Override
    public ConsortiaThrowRank trash() {

        return new ConsortiaThrowRank("", -1, 0, 0, -1, -1);
    }

}

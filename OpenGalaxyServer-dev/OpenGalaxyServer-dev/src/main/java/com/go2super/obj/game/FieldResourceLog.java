package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;

@Data
public class FieldResourceLog extends BufferObject {

    private long userId;

    private String name;

    private int guid;
    private int gas;
    private int money;
    private int metal;

    public FieldResourceLog(long userId, String name, int guid, int gas, int money, int metal) {

        this.userId = userId;

        this.name = name;

        this.guid = guid;
        this.gas = gas;
        this.money = money;
        this.metal = metal;

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(userId);

        go2buffer.addString(name, 32);

        go2buffer.addInt(guid);
        go2buffer.addInt(gas);
        go2buffer.addInt(money);
        go2buffer.addInt(metal);

    }

    @Override
    public FieldResourceLog trash() {

        return new FieldResourceLog(0, "", 0, 0, 0, 0);
    }


}

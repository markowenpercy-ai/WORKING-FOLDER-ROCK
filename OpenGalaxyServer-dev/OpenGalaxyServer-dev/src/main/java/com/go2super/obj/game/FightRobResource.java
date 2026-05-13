package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FightRobResource extends BufferObject {

    private long userId;
    private int headId;

    private int metal;
    private int gas;
    private int money;

    private String roleName;

    public FightRobResource(long userId, int headId, int metal, int gas, int money, String roleName) {

        this.userId = userId;
        this.headId = headId;
        this.metal = metal;
        this.gas = gas;
        this.money = money;
        this.roleName = roleName;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(userId);
        go2buffer.addInt(headId);

        go2buffer.addUnsignInt(metal);
        go2buffer.addUnsignInt(gas);
        go2buffer.addUnsignInt(money);

        go2buffer.addString(roleName, 32);

    }

    @Override
    public FightRobResource trash() {

        return new FightRobResource(-1, 0, 0, 0, 0, "");
    }

}

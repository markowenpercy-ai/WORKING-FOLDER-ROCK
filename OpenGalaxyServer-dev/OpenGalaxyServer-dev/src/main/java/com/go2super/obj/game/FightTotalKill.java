package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FightTotalKill extends BufferObject {

    private long userId;

    private int num;
    private int bodyId;
    private int reserve;

    private String modelName;
    private String roleName;

    public FightTotalKill(long userId, int num, int bodyId, int reserve, String modelName, String roleName) {

        this.userId = userId;
        this.num = num;
        this.bodyId = bodyId;
        this.reserve = reserve;
        this.modelName = modelName;
        this.roleName = roleName;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(userId);
        go2buffer.addInt(num);
        go2buffer.addUnsignShort(bodyId);
        go2buffer.addShort(reserve);

        go2buffer.addString(modelName, 32);
        go2buffer.addString(roleName, 32);

    }

    @Override
    public FightTotalKill trash() {

        return new FightTotalKill(-1, 0, 0, 0, "", "");
    }

}

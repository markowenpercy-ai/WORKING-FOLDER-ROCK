package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FightTotalExp extends BufferObject {

    private long userId;
    private long commanderUserId;

    private int exp;
    private int headId;
    private int levelId;

    private String commanderName;
    private String roleName;

    public FightTotalExp(long userId, long commanderUserId, int exp, int headId, int levelId, String commanderName, String roleName) {

        this.userId = userId;
        this.commanderUserId = commanderUserId;
        this.exp = exp;
        this.headId = headId;
        this.levelId = levelId;
        this.commanderName = commanderName;
        this.roleName = roleName;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(userId);
        go2buffer.addLong(commanderUserId);

        go2buffer.addUnsignInt(exp);
        go2buffer.addShort(headId);
        go2buffer.addUnsignShort(levelId);

        go2buffer.addString(commanderName, 32);
        go2buffer.addString(roleName, 32);

    }

    @Override
    public FightTotalExp trash() {

        return new FightTotalExp(-1, -1, 0, 0, 0, "", "");
    }

}

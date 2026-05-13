package com.go2super.packet.rank;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.SmartString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WarFieldPage extends BufferObject {
    public long userId;
    public String name;
    public int guid;
    public int warScore;
    private int warKilldown;
    private int warWin;


    @Override
    public void write(Go2Buffer go2buffer) {
        go2buffer.pushByte((8 - go2buffer.position() % 8) % 8);
        go2buffer.addLong(userId);
        go2buffer.addString(name, 32);
        go2buffer.addInt(guid);
        go2buffer.addUnsignInt(warScore);
        go2buffer.addUnsignInt(warKilldown);
        go2buffer.addUnsignShort(warWin);
    }

    @Override
    public WarFieldPage trash() {
        return new WarFieldPage(-1, "", 1, 2, 3, 4);
    }
}

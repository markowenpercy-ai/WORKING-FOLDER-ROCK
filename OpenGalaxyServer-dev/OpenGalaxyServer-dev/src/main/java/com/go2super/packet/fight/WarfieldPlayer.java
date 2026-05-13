package com.go2super.packet.fight;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarfieldPlayer extends BufferObject {
    public long userId;
    public int guid;
    public String name;

    @Override
    public void write(Go2Buffer go2buffer) {
        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);
        go2buffer.addLong(userId);
        go2buffer.addInt(guid);
        go2buffer.addString(name, 32);
    }

}

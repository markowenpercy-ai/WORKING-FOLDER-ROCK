package com.go2super.packet.igl;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RacingRank extends BufferObject {
    public String name;
    public long userId;
    public int gameServerId;
    public int rankId;


    @Override
    public void write(Go2Buffer go2buffer) {
        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);
        go2buffer.addString(name, 32);
        go2buffer.addLong(userId);
        go2buffer.addInt(gameServerId);
        go2buffer.addInt(rankId);
    }
}

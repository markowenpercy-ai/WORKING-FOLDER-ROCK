package com.go2super.obj.game;

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
public class ArenaPageInfo extends BufferObject {

    public long sourceUserId;
    public long targetUserId;

    private String sourceName;
    private String targetName;

    private int sourceGuid;
    private int targetGuid;

    private int sourceShipNum;
    private int targetShipNum;

    private int passKey;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(sourceUserId);
        go2buffer.addLong(targetUserId);

        go2buffer.addString(sourceName, 32);
        go2buffer.addString(targetName, 32);

        go2buffer.addInt(sourceGuid);
        go2buffer.addInt(targetGuid);

        go2buffer.addUnsignInt(sourceShipNum);
        go2buffer.addUnsignInt(targetShipNum);

        go2buffer.addChar(passKey);

    }

    @Override
    public ArenaPageInfo trash() {

        return new ArenaPageInfo();
    }

}
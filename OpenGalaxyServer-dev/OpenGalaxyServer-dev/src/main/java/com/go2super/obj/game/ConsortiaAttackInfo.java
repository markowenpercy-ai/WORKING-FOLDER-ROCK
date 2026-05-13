package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class ConsortiaAttackInfo extends BufferObject {

    private String sourceName;
    private String consortiaName;
    private String targetName;

    private long sourceUserId;
    private long targetUserId;

    private int targetGuid;
    private int targetGalaxyId;

    private int sourceGuid;
    private int sourceGalaxyId;

    private int needTime;
    private int reserve;

    public ConsortiaAttackInfo(String sourceName, String consortiaName, String targetName, long sourceUserId, long targetUserId, int targetGuid, int targetGalaxyId, int sourceGuid, int sourceGalaxyId, int needTime, int reserve) {

        this.sourceName = sourceName;
        this.consortiaName = consortiaName;
        this.targetName = targetName;
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.targetGuid = targetGuid;
        this.targetGalaxyId = targetGalaxyId;
        this.sourceGuid = sourceGuid;
        this.sourceGalaxyId = sourceGalaxyId;
        this.needTime = needTime;
        this.reserve = reserve;

    }

    @Override
    public void write(Go2Buffer go2Buffer) {

        go2Buffer.pushByte((8 - go2Buffer.getBuffer().position() % 8) % 8);

        go2Buffer.addString(sourceName, 32);
        go2Buffer.addString(consortiaName, 32);
        go2Buffer.addString(targetName, 32);

        go2Buffer.addLong(sourceUserId);
        go2Buffer.addLong(targetUserId);

        go2Buffer.addInt(targetGuid);
        go2Buffer.addInt(targetGalaxyId);

        go2Buffer.addInt(sourceGuid);
        go2Buffer.addInt(sourceGalaxyId);

        go2Buffer.addInt(needTime);
        go2Buffer.addInt(reserve);

    }

    @Override
    public ConsortiaAttackInfo trash() {

        return new ConsortiaAttackInfo("", "", "", -1, -1, -1, -1, -1, -1, -1, -1);
    }

}

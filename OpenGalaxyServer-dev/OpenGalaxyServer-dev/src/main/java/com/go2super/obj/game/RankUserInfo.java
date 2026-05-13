package com.go2super.obj.game;

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
public class RankUserInfo extends BufferObject {

    private SmartString name = SmartString.of("", 32);
    private SmartString consortiaName = SmartString.of("", 32);

    private long userId;

    private int assault;
    private int rankId;
    private int killTotal;
    private int guid;

    private int consortiaId;
    private int headId;
    private int level;
    private int reserve;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.position() % 8) % 8);

        go2buffer.addString(name);
        go2buffer.addString(consortiaName);

        go2buffer.addLong(userId);

        go2buffer.addInt(assault);
        go2buffer.addInt(rankId);
        go2buffer.addInt(killTotal);
        go2buffer.addInt(guid);
        go2buffer.addInt(consortiaId);
        go2buffer.addShort(headId);
        // BotLogger.log("GUID: " + guid + ", LEVEL: " + level);
        go2buffer.addUnsignChar(level);
        go2buffer.addChar(reserve);

    }

    @Override
    public RankUserInfo trash() {

        return new RankUserInfo(SmartString.of("", 32), SmartString.of("", 32), 0, 0, 0, 0, 0, 0, 0, 0, 0);
    }

}

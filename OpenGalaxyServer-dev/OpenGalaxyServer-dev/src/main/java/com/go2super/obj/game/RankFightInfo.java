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
public class RankFightInfo extends BufferObject {

    private SmartString name = SmartString.of("", 32);
    private SmartString consortiaName = SmartString.of("", 32);

    private long userId;

    private int guid;
    private int galaxyId;
    private int starType;

    private int reserve;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.position() % 8) % 8);

        go2buffer.addString(consortiaName);
        go2buffer.addString(name);

        go2buffer.addLong(userId);

        go2buffer.addInt(guid);
        go2buffer.addInt(galaxyId);
        go2buffer.addInt(starType);
        go2buffer.addInt(reserve);

    }

    @Override
    public RankFightInfo trash() {

        return new RankFightInfo(SmartString.of("", 32), SmartString.of("", 32), -1, -1, -1, 0, 0);
    }

}

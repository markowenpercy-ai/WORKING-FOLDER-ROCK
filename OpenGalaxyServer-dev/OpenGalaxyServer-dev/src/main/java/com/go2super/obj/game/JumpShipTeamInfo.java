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
public class JumpShipTeamInfo extends BufferObject {

    private long userId;
    private String userName = "";

    private int shipTeamId;
    private int fromGalaxyId;
    private int toGalaxyId;

    private int spareTime;
    private int totalTime;

    private int fromGalaxyMapId;
    private int toGalaxyMapId;

    private byte kind;
    private byte galaxyType;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((8 - go2buffer.getBuffer().position() % 8) % 8);

        go2buffer.addLong(userId);
        go2buffer.addString(userName, 32);

        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(fromGalaxyId);
        go2buffer.addInt(toGalaxyId);

        go2buffer.addInt(spareTime);
        go2buffer.addInt(totalTime);

        go2buffer.addChar(fromGalaxyMapId);
        go2buffer.addChar(toGalaxyMapId);

        go2buffer.addChar(kind);
        go2buffer.addChar(galaxyType);

    }

    @Override
    public JumpShipTeamInfo trash() {

        return new JumpShipTeamInfo();
    }

}
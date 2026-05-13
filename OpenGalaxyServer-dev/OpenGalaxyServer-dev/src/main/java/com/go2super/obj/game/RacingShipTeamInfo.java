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
public class RacingShipTeamInfo extends BufferObject {

    public String teamName;

    private int shipTeamId;
    private int commanderId;
    private int bodyId;
    private int shipNum;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addString(teamName, 32);
        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(commanderId);
        go2buffer.addShort((short) bodyId);
        go2buffer.addUnsignShort(shipNum);

    }

    @Override
    public RacingShipTeamInfo trash() {

        return new RacingShipTeamInfo();
    }

}
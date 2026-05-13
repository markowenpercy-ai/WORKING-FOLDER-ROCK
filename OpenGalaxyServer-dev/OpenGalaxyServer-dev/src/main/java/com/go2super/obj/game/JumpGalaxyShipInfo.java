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
public class JumpGalaxyShipInfo extends BufferObject {

    private String teamName = "";

    private int shipTeamId = -1;
    private int jumpNeedTime;
    private int shipNum;

    private int gas;
    private int commanderId;

    private int bodyId;
    private int gasPercent;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);
        go2buffer.addString(teamName, 32);

        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(jumpNeedTime);
        go2buffer.addInt(shipNum);

        go2buffer.addUnsignInt(gas);
        go2buffer.addInt(commanderId);
        go2buffer.addShort((short) bodyId);
        go2buffer.addShort((short) gasPercent);

    }

    @Override
    public JumpGalaxyShipInfo trash() {

        return new JumpGalaxyShipInfo();
    }

}
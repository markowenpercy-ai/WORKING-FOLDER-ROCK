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
public class GalaxyFleetInfo extends BufferObject {

    private int shipTeamId;
    private int shipNum;

    private short bodyId;
    private short reserve;

    private byte direction;
    private byte posX;
    private byte posY;

    // 0 = Attacker (Red)
    // 2 = Mine (Magenta)
    // 3 = Defender (Blue)
    private byte owner;

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(shipTeamId);
        go2buffer.addInt(shipNum);

        go2buffer.addShort(bodyId);
        go2buffer.addShort(reserve);

        go2buffer.addByte(direction);
        go2buffer.addByte(posX);
        go2buffer.addByte(posY);

        go2buffer.addByte(owner);

    }

    @Override
    public GalaxyFleetInfo trash() {

        return new GalaxyFleetInfo();
    }

}
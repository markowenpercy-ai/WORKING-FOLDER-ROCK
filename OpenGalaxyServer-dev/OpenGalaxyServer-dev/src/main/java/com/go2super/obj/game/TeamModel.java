package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import com.go2super.obj.utility.VariableType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamModel extends BufferObject {

    private List<ShipTeamNum> model = new ArrayList<>(VariableType.MAX_SHIPTEAMBODY);

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        for (int i = 0; i < 9; i++) {

            go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

            if (model.size() > i) {

                ShipTeamNum num = model.get(i);

                go2buffer.addInt(num.getShipModelId());
                go2buffer.addInt(num.getNum());

            } else {

                go2buffer.addInt(-1);
                go2buffer.addInt(-1);

            }
        }
    }

    @Override
    public void read(Go2Buffer go2buffer) {

        go2buffer.getByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        for (int i = 0; i < 9; i++) {

            go2buffer.getByte((4 - go2buffer.getBuffer().position() % 4) % 4);

            ShipTeamNum teamNum = new ShipTeamNum(go2buffer.getInt(), go2buffer.getInt());

            model.add(teamNum);

        }

    }

    @Override
    public ShipTeamBody trash() {

        return new ShipTeamBody();
    }

}

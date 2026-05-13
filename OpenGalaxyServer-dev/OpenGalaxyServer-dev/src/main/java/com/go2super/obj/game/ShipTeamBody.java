package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.ShipModel;
import com.go2super.obj.BufferObject;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.service.PacketService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShipTeamBody extends BufferObject  implements Serializable {

    public List<ShipTeamNum> cells = new ArrayList<>();

    public int getRenderedBody() {

        return getBestHull().getId();
    }

    public ViewTeamModel getViewTeamModel() {

        return new ViewTeamModel(cells);
    }

    public TeamModel getTeamModel() {

        return new TeamModel(cells);
    }

    public BodyLevelMeta getBestHull() {

        BodyLevelMeta best = null;

        for (ShipTeamNum teamNum : cells) {

            ShipModel model = PacketService.getShipModel(teamNum.getShipModelId());

            if (model == null) {
                continue;
            }

            BodyLevelMeta current = ResourceManager.getShipBodies().getMeta(model.getBodyId());

            if (best != null && current.getPriority() < best.getPriority()) {
                continue;
            }

            best = current;

        }

        return best;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        for (int i = 0; i < 9; i++) {

            // go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

            if (cells.size() > i) {

                ShipTeamNum num = cells.get(i);

                go2buffer.addInt(num.getShipModelId());
                go2buffer.addInt(num.getBodyId());
                go2buffer.addInt(num.getNum());

            } else {

                go2buffer.addInt(-1);
                go2buffer.addInt(0);
                go2buffer.addInt(0);

            }

        }

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        for (int i = 0; i < 9; i++) {

            go2buffer.getByte((4 - go2buffer.getBuffer().position() % 4) % 4);

            ShipTeamNum teamNum = new ShipTeamNum(go2buffer.getInt(), go2buffer.getInt());
            cells.add(teamNum);

        }

    }

    @Override
    public ShipTeamBody trash() {

        return new ShipTeamBody();
    }

}
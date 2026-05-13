package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.ShipModel;
import com.go2super.obj.BufferObject;
import com.go2super.service.PacketService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ShipTeamNum extends BufferObject implements Serializable {

    public int shipModelId;
    public int num;

    public ShipTeamNum(int shipModelId, int num) {

        this.shipModelId = shipModelId;
        this.num = num;

    }

    public int getBodyId() {

        ShipModel model = PacketService.getShipModel(shipModelId);

        if (model == null) {
            return 0;
        }

        return model.getBodyId();

    }

    public int getMinRange() {

        return getModel().getMinRange();
    }

    public int getShields() {

        return getModel().getShields();
    }

    public int getStructure() {

        return getModel().getStructure();
    }

    public ShipModel getModel() {

        return PacketService.getShipModel(shipModelId);
    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(shipModelId);
        go2buffer.addInt(num);

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        shipModelId = go2buffer.getInt();
        num = go2buffer.getInt();

    }

    @Override
    public ShipTeamNum trash() {

        return new ShipTeamNum(-1, 0);
    }

}
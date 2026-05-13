package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.database.entity.ShipModel;
import com.go2super.obj.BufferObject;
import com.go2super.service.PacketService;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BruiseShipInfo extends BufferObject {

    public int shipModelId;
    public int num;

    public BruiseShipInfo(int shipModelId, int num) {

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

        go2buffer.pushByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        go2buffer.addInt(shipModelId);
        go2buffer.addInt(num);

    }

    @Override
    public void read(Go2Buffer go2buffer) {

        go2buffer.getByte((4 - go2buffer.getBuffer().position() % 4) % 4);

        shipModelId = go2buffer.getInt();
        num = go2buffer.getInt();

    }

    @Override
    public BruiseShipInfo trash() {

        return new BruiseShipInfo(-1, 0);
    }

}
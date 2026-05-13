package com.go2super.obj.game;

import com.go2super.buffer.Go2Buffer;
import com.go2super.obj.BufferObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ShipBodyInfo extends BufferObject {

    public int bodyPartId;
    public int needTime;

    public ShipBodyInfo(int bodyPartId, int needTime) {

        this.bodyPartId = bodyPartId;
        this.needTime = needTime;

    }

    @Override
    public void write(Go2Buffer go2buffer) {

        go2buffer.addInt(bodyPartId);
        go2buffer.addInt(needTime);

    }

}
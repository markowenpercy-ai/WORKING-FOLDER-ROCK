package com.go2super.database.entity.sub;

import com.go2super.obj.game.CreateShipInfo;
import com.go2super.socket.util.DateUtil;
import lombok.Data;

import java.util.*;

@Data
public class ShipBodyInfo {

    private int shipModelId;
    private Date until;

    private int num;
    private int buildTime;
    private int incSpeed;

    public ShipBodyInfo(int shipModelId, int num, int buildTime) {

        this.shipModelId = shipModelId;
        this.num = num;

        this.buildTime = buildTime;
        this.until = DateUtil.now(buildTime);

    }

    public CreateShipInfo packet() {

        int remains = until != null ? DateUtil.remains(until).intValue() : 0;
        return new CreateShipInfo(shipModelId, remains + ((num - 1) * buildTime), num, incSpeed);
    }

    public static ShipBodyInfo of(int shipModelId, int num, int buildTime) {

        return new ShipBodyInfo(shipModelId, num, buildTime);
    }

}

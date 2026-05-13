package com.go2super.database.entity.sub;

import com.go2super.obj.game.CreateShipInfo;
import com.go2super.socket.util.DateUtil;
import lombok.Data;

import java.util.*;

@Data
public class FactoryShip {

    private int shipModelId;
    private Date until;

    private int num;
    private double buildTime;
    private double incSpeed;

    public FactoryShip(int shipModelId, int num, double buildTime) {

        this.shipModelId = shipModelId;
        this.num = num;

        this.buildTime = buildTime;
        this.until = DateUtil.now((int) buildTime);

    }

    public CreateShipInfo packet() {
        return new CreateShipInfo(shipModelId, needTime(), num, (int) incSpeed);
    }

    public static FactoryShip of(int shipModelId, int num, double buildTime) {

        return new FactoryShip(shipModelId, num, buildTime);
    }

    public int needTime() {
        int remains = until != null ? DateUtil.remains(until).intValue() : 0;
        double totalBuildTime = buildTime * (num - 1);
        return remains + (int) totalBuildTime;
    }

}

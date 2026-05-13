package com.go2super.database.entity.sub;

import lombok.Data;

@Data
public class BruiseShip {

    private int shipModelId;
    private int num;

    public BruiseShip(int shipModelId, int num) {

        this.shipModelId = shipModelId;
        this.num = num;

    }

    public static BruiseShip of(int shipModelId, int num) {

        return new BruiseShip(shipModelId, num);
    }

}

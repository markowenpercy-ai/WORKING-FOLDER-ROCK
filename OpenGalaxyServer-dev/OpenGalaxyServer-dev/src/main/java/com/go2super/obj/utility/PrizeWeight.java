package com.go2super.obj.utility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrizeWeight {

    private int propId;
    private int amount;

    private int he3;
    private int gold;
    private int metal;
    private int vouchers;

    private int weight;

    public static PrizeWeight of(int propId, int amount, int weight, int he3, int gold, int metal, int vouchers) {

        return new PrizeWeight(propId, amount, he3, gold, metal, vouchers, weight);
    }

}

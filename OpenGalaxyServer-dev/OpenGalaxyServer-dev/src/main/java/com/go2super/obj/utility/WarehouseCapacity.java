package com.go2super.obj.utility;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WarehouseCapacity {

    private int goldCapacity;
    private int he3Capacity;
    private int metalCapacity;

}

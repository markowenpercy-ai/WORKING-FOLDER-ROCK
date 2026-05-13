package com.go2super.dto;

import com.go2super.database.entity.sub.storeevent.PurchaseLimit;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class BuyEventInfoDTO {
    private int storePoints;
    private List<PurchaseLimit> purchased;
}

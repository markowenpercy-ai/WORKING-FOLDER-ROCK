package com.go2super.database.entity.sub.storeevent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class PurchaseLimit {
    private int id;
    private int quantity;
}

package com.go2super.database.entity.sub.storeevent;

import com.go2super.database.entity.sub.StoreEventEntry;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BuyEventEntry extends StoreEventEntry {
    public BuyEventEntry(String eventId) {
        super(eventId);

        this.limits = new ArrayList<>();
    }

    private List<PurchaseLimit> limits;

    public int getLimit(int packId) {
        for (PurchaseLimit limit : limits) {
            if (packId == limit.getId()) {
                return limit.getQuantity();
            }
        }
        return 0;
    }

    public void addToLimit(int packId, int quantity) {
        for (PurchaseLimit limit : limits) {
            if (packId == limit.getId()) {
                limit.setQuantity(limit.getQuantity() + quantity);
                return;
            }
        }

        limits.add(new PurchaseLimit(packId, quantity));
    }
}

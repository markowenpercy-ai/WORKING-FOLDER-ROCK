package com.go2super.logger.lookup;

import com.go2super.logger.lookup.sub.InventoryLookup;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInventoryLookup {

    private InventoryLookup userInventory;
    private InventoryLookup corpInventory;

}

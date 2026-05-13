package com.go2super.logger.lookup;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FullUserLookup {

    private UserInventoryLookup userInventory;

}

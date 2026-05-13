package com.go2super.logger.lookup.sub;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommanderChipLookup {

    private String chipName;
    private int chipSlot;
    private int chipId;

}

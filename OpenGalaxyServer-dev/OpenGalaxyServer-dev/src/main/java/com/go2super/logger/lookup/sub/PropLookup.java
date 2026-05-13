package com.go2super.logger.lookup.sub;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PropLookup {

    private String propName;
    private int propId;

    private int propUnlockedNum;
    private int propLockedNum;

}

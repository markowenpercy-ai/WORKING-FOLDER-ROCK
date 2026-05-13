package com.go2super.logger.lookup.sub;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommanderGemLookup {

    private String gemName;
    private int gemId;

}

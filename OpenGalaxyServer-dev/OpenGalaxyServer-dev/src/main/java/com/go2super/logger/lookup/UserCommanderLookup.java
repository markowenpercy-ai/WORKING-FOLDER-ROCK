package com.go2super.logger.lookup;

import com.go2super.logger.lookup.sub.CommanderLookup;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class UserCommanderLookup {

    private List<CommanderLookup> commanders;

}

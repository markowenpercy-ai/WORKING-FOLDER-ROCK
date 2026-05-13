package com.go2super.logger.lookup.sub;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class InventoryLookup {

    private List<PropLookup> propLookups;

}

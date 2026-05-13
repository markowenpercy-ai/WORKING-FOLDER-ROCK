package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.MatchType;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class FleetMatch  implements Serializable {

    private String match;
    private MatchType matchType;

    private int galaxyId;

}

package com.go2super.database.entity.sub;

import com.go2super.database.entity.RiskIncident;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class BadGuidIncident extends RiskIncident {

    private String email;
    private String discord;
    private String accountName;
    private String username;

    private int guid;
    private long userId;

    private int targetGuid;
    private int totalCount;
    private List<String> lastDetections;

    private Date lastDetection;
    private String accountId;

}

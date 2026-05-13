package com.go2super.database.entity.sub;

import com.go2super.database.entity.RiskIncident;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class SameIPIncident extends RiskIncident {

    private String ip;

    private List<UserSameIPIncidentInfo> users = new ArrayList<>();

}

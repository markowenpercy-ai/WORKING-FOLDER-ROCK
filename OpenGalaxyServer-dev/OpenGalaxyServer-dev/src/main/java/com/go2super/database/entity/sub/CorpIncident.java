package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.CorpIncidentType;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class CorpIncident {

    private CorpIncidentType type;
    private Date date;

    private String sourceName;
    private String objectName;

    private Long sourceUserId;
    private Long sourceObjectId;

    private int guid;
    private int extend;
    private int incidentType;

}

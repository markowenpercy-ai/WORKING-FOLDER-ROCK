package com.go2super.database.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_risk_incidents")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class RiskIncident {

    @Id
    private ObjectId id;

    private boolean ignore;
    private Date creation;
    private String creator;

}

package com.go2super.database.entity;

import com.go2super.database.entity.type.SanctionType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_sanctions")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Sanction {

    @Id
    private ObjectId id;
    private SanctionType sanctionType;

    private String label;
    private Date creation;

    private String staffAccountId;
    private String staffName;
    private int staffGuid;

    private String userAccountId;
    private String userName;
    private int userGuid;

}

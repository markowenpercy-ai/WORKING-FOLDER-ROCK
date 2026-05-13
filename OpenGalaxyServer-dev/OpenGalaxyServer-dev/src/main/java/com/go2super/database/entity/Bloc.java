package com.go2super.database.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_blocs")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class Bloc {

    @Id
    private ObjectId id;

    private Date creation;
    private String name;
    private int organizer;

    private String code;
    private Date until;

}

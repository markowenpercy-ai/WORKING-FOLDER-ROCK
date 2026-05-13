package com.go2super.database.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

@Document(collection = "game_increments")
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class AutoIncrement {

    @Id
    private ObjectId id;

    private String name;
    private int current;

    private boolean toSave;

    public void save() {

        toSave = true;
    }

}

package com.go2super.database.entity;

import com.go2super.obj.type.BonusType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_boosts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameBoost {

    @Id
    private ObjectId id;

    private int propId;
    private int mimeType;
    private List<BonusType> bonuses = new ArrayList<>();

    private int seconds;

}

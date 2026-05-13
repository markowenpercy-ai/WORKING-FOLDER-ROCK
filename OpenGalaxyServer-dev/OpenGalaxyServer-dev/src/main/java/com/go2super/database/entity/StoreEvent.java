package com.go2super.database.entity;

import com.go2super.database.entity.sub.StoreEventEntry;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_store_events")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreEvent {
    @Id
    private ObjectId id;
    private String accountId;

    private long storePoints;

    private List<StoreEventEntry> events;
}

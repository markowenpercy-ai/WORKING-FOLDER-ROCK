package com.go2super.database.entity.sub;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class StoreEventEntry {
    public StoreEventEntry(String eventId) {
        this.guid = eventId;
    }

    private String guid;
}

package com.go2super.database.entity;

import com.go2super.obj.game.ShipTeamNum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Document(collection = "fleet_presets")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FleetPreset {

    @javax.persistence.Id
    private ObjectId id;

    private int userId;
    private String name;
    private List<FleetPresetEntry> entries;
    private Date createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FleetPresetEntry implements Serializable {
        private String name;
        private int he3;
        private int commanderId;
        private int bodyId;
        private int rangeType;
        private int preferenceType;
        private int posX;
        private int posY;
        private int direction;
        private List<ShipTeamNum> cells;
    }
}

package com.go2super.database.entity;

import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.service.BattleService;
import com.go2super.service.GalaxyService;
import com.go2super.service.PacketService;
import com.go2super.service.battle.match.WarMatch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_planets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Planet {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private long userId;

    private PlanetType type;
    private GalaxyTile position;

    public List<Fleet> getFleets() {

        return PacketService.getInstance().getFleetCache().findAllByGalaxyId(position.galaxyId());
    }

    public boolean isInWar() {

        Optional<WarMatch> optionalWar = BattleService.getInstance().getWar(this);
        return optionalWar.isPresent() && !optionalWar.get().getPause().get();
    }

    public void save() {

        GalaxyService.getInstance().getPlanetCache().save(this);
    }

}

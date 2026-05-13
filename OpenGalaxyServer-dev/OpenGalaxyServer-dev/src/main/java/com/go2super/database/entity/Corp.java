package com.go2super.database.entity;

import com.go2super.database.entity.sub.*;
import com.go2super.obj.game.ConsortiaJobName;
import com.go2super.resources.data.meta.FortificationEffectMeta;
import com.go2super.service.CorpService;
import com.go2super.service.GalaxyService;
import com.go2super.socket.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Column;
import javax.persistence.Id;
import java.util.*;

@Document(collection = "game_corps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Corp {

    @Id
    private ObjectId id;

    @Column(unique = true)
    private int corpId;

    private int contribution;

    private int maxMembers;
    private int rbpLimit;

    private double resourceBonus;
    private double mergeBonus;

    private int contributionMerge;
    private int contributionMall;

    private int wealth;
    private int icon;

    @Column(unique = true)
    private String name;
    @Column(unique = true)
    private ObjectId blocId;

    private String acronym;
    private String philosophy;
    private String bulletin;

    private int planets;
    private int territories;

    private int level;
    private int mallLevel;
    private int mergingLevel;
    private int warehouseLevel;

    private int fees;

    private int piratesLevel;
    private Date lastPirates;

    private ConsortiaJobName consortiaJobName;

    @Field(name = "corp_history")
    private CorpHistory history;
    @Field(name = "corp_members")
    private CorpMembers members;
    @Field(name = "corp_upgrade")
    private CorpUpgrade corpUpgrade;
    @Field(name = "corp_territories")
    private CorpTerritories corpTerritories;

    public double getRBPBonus() {

        double total = 0.0d;
        for (ResourcePlanet rbp : getResourcePlanets()) {
            RBPBuilding spaceStation = rbp.getSpaceStation();
            if (spaceStation == null) {
                continue;
            }
            Optional<FortificationEffectMeta> effectMeta = spaceStation.getLevelData().getEffect("affect");
            if (effectMeta.isEmpty()) {
                continue;
            }
            total += effectMeta.get().getValue();
        }
        return total;
    }

    public List<ResourcePlanet> getResourcePlanets() {

        return GalaxyService.getInstance().getPlanetCache().findResourcePlanets(corpId);
    }

    public int getPiratesNum() {

        if (lastPirates == null || !DateUtil.sameDay(new Date(), lastPirates)) {
            return 0;
        }
        return 1;
    }

    public void save() {

        CorpService.getInstance().getCorpCache().save(this);
    }

}

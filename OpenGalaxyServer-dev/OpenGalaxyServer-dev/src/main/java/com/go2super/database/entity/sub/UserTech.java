package com.go2super.database.entity.sub;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ResearchData;
import com.go2super.resources.data.meta.ResearchEffectMeta;
import com.go2super.resources.data.meta.ResearchLevelMeta;
import lombok.*;

import java.io.Serializable;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserTech implements Serializable {

    private int id;
    private int level;

    public ResearchData getResearchData() {

        return ResourceManager.getScience().getResearchData(id);
    }

    public ResearchLevelMeta getLevelMeta() {

        return getResearchData().getLevel(level);
    }

    public ResearchEffectMeta getEffect(String name) {

        return getLevelMeta().getEffects().stream().filter(researchEffectMeta -> researchEffectMeta.getType().equals(name)).findAny().orElse(null);
    }

    public double getEffectValue(String name) {

        return getEffect(name).getValue();
    }

}

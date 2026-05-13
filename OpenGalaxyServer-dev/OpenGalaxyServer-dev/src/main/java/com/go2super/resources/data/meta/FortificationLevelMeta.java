package com.go2super.resources.data.meta;

import com.go2super.database.entity.type.SpaceFortAttackType;
import com.go2super.database.entity.type.SpaceFortDefendType;
import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FortificationLevelMeta extends JsonData {

    private int lv;
    private List<FortificationEffectMeta> effects;

    public int getEffectValue(String name) {

        Optional<FortificationEffectMeta> effect = getEffect(name);
        if (effect.isPresent()) {
            return (int) effect.get().getValue();
        }
        return -1;
    }

    public Optional<Integer> getWealthRequired() {

        Optional<FortificationEffectMeta> wealthEffect = getEffect("wealth");
        if (wealthEffect.isPresent()) {
            return Optional.of((int) wealthEffect.get().getValue());
        }

        Optional<FortificationEffectMeta> needRichesEffect = getEffect("needRiches");
        if (needRichesEffect.isPresent()) {
            return Optional.of((int) needRichesEffect.get().getValue());
        }

        return Optional.empty();

    }

    public Optional<FortificationEffectMeta> getEffect(String name) {

        return effects.stream().filter(effect -> effect.getType().equals(name)).findFirst();
    }

    public SpaceFortDefendType getDefendType() {

        return SpaceFortDefendType.getByCode(getEffectValue("defendType"));
    }

    public SpaceFortAttackType getAttackType() {

        return SpaceFortAttackType.getByCode(getEffectValue("attackType"));
    }

}

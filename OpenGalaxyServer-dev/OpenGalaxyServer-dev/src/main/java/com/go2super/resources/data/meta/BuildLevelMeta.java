package com.go2super.resources.data.meta;

import com.go2super.database.entity.User;
import com.go2super.database.entity.sub.UserBuildings;
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
public class BuildLevelMeta extends JsonData {

    private int lv;
    private int time;

    private double metal;
    private double gas;
    private double gold;

    private int exp;

    private List<BuildRequirementMeta> require;
    private List<BuildEffectMeta> effects;

    public List<String> getEffectNames() {

        List<String> names = new ArrayList<>();
        for (BuildEffectMeta meta : effects) {
            names.add(meta.getType());
        }
        return names;
    }

    public BuildEffectMeta getEffect(String type) {

        for (BuildEffectMeta meta : effects) {
            if (meta.getType().equalsIgnoreCase(type)) {
                return meta;
            }
        }
        return null;
    }

    public boolean canBuild(User user, double decreaseMetal, double decreaseGold, double decreaseHe3) {

        UserBuildings buildings = user.getBuildings();

        int gas = (int) Math.floor(this.gas * (1 - (decreaseHe3 * 0.01)));
        int metal = (int) Math.floor(this.metal * (1 - (decreaseMetal * 0.01)));
        int gold = (int) Math.floor(this.gold * (1 - (decreaseGold * 0.01)));

        if (user.getResources().getHe3() < gas ||
            user.getResources().getMetal() < metal ||
            user.getResources().getGold() < gold) {
            return false;
        }

        if (buildings != null && require != null) {
            for (BuildRequirementMeta requirement : require) {
                if (!buildings.has(requirement.getBuild(), requirement.getLv())) {
                    return false;
                }
            }
        }

        return true;

    }

    public void charge(User user, double decreaseMetal, double decreaseGold, double decreaseHe3) {

        int gas = (int) Math.floor(this.gas * (1 - (decreaseHe3 * 0.01)));
        int metal = (int) Math.floor(this.metal * (1 - (decreaseMetal * 0.01)));
        int gold = (int) Math.floor(this.gold * (1 - (decreaseGold * 0.01)));

        user.getResources().setHe3(user.getResources().getHe3() - gas);
        user.getResources().setMetal(user.getResources().getMetal() - metal);
        user.getResources().setGold(user.getResources().getGold() - gold);

    }


}

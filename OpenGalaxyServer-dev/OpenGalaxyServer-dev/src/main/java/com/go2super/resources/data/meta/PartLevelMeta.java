package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.props.PropPartData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PartLevelMeta extends JsonData {

    private int id;
    private int lv;

    private BuildCostMeta buildCost;

    private double fuelUsage;
    private int moduleUsage;

    private List<PartEffectMeta> effects;
    private UpgradePartMeta upgrade;

    public boolean hasRequirements(List<Integer> currentParts) {

        PropPartData propData = getPropData();
        return propData.canUse(currentParts);
    }

    public double getEffectMax(String attribute) {

        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? meta.getMax() : 0;
    }

    public double getEffectMin(String attribute) {

        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? meta.getMin() : 0;
    }

    public double getEffectValue(String attribute) {

        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? (double) meta.getValue() : 0;
    }

    public String getEffectString(String attribute) {

        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? (String) meta.getValue() : null;
    }

    public PartSpecialMeta[] getSpecialEffect(String attribute) {

        PartEffectMeta meta = getEffect(attribute);

        if (meta == null) {
            return null;
        }

        PartSpecialMeta[] customMeta = new GsonBuilder().create().fromJson(new Gson().toJson(meta.getValue()), PartSpecialMeta[].class);
        return meta != null ? customMeta : new PartSpecialMeta[0];

    }

    public PartBonusesMeta getPartBonus(String attribute) {
        PartEffectMeta meta = getEffect(attribute);

        if (meta == null) {
            return null;
        }

        PartBonusMeta[] x  = new GsonBuilder().create().fromJson(new Gson().toJson(meta.getValue()), PartBonusMeta[].class);
        PartBonusesMeta bonusesMeta = new PartBonusesMeta();
        bonusesMeta.setBonuses(new ArrayList<>());
        bonusesMeta.setType(meta.getType());
        for (PartBonusMeta bonusMeta : x) {
            bonusesMeta.getBonuses().add(bonusMeta);
        }
        return bonusesMeta;
    }

    public PartEffectMeta getEffect(String attribute) {

        for (PartEffectMeta meta : effects) {
            if (meta.getType().equalsIgnoreCase(attribute)) {
                return meta;
            }
        }
        return null;
    }

    public boolean hasEffect(String attribute) {

        return getEffect(attribute) != null;
    }

    public ShipPartData getData() {

        return ResourceManager.getShipParts().findByPartId(id);
    }

    public PropPartData getPropData() {

        return getData().getPropData();
    }

}

package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.props.PropBodyData;
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
public class BodyLevelMeta extends JsonData {

    private int id;
    private int lv;
    private int priority;

    private BuildCostMeta buildCost;
    private TransmissionMeta transmission;

    private double shield;
    private double armor;
    private double agility;
    private double daedalus;
    private double stability;
    private double movement;
    private double effectiveStack;
    private double fuelStorage;
    private double moduleStorage;

    private List<PartEffectMeta> effects;
    private UpgradeMeta upgrade;

    public boolean hasRequirements(List<Integer> currentBodies) {

        PropBodyData propData = getPropData();
        return propData.canUse(currentBodies);
    }

    public PartSpecialMeta[] getSpecialEffect(String attribute) {

        PartEffectMeta meta = getEffect(attribute);

        if (meta == null) {
            return null;
        }

        PartSpecialMeta[] customMeta = new GsonBuilder().create().fromJson(new Gson().toJson(meta.getValue()), PartSpecialMeta[].class);
        return meta != null ? customMeta : new PartSpecialMeta[0];

    }

    public PartBonusMeta[] getBonusEffect(String attribute) {

        PartEffectMeta meta = getEffect(attribute);

        if (meta == null) {
            return null;
        }

        return new GsonBuilder().create().fromJson(new Gson().toJson(meta.getValue()), PartBonusMeta[].class);

    }

    public PartEffectMeta getEffect(String attribute) {

        for (PartEffectMeta meta : effects) {
            if (meta.getType().equalsIgnoreCase(attribute)) {
                return meta;
            }
        }
        return null;
    }

    public double getEffectValueAsDouble(String attribute) {

        PartEffectMeta meta = getEffect(attribute);
        return meta != null ? (double) meta.getValue() : 0;
    }

    public boolean hasEffect(String attribute) {

        return getEffect(attribute) != null;
    }

    public PropBodyData getPropData() {

        return getBodyData().getPropData();
    }

    public ShipBodyData getBodyData() {

        return ResourceManager.getShipBodies().findByBodyId(id);
    }

}

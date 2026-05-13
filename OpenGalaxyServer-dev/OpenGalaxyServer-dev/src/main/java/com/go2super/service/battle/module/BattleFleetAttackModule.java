package com.go2super.service.battle.module;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.service.battle.calculator.ShipTechs;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class BattleFleetAttackModule extends ShipModule implements Serializable {

    private String damageType;
    private double intercept;
    private double hitRate;
    private double critRate;
    private double steering;

    private int minAttack;
    private int maxAttack;

    private int maxRange;
    private int minRange;

    public String getDamageType(){
        if(damageType == null){
            return "fort";
        }
        return damageType;
    }

    public PartLevelMeta getMeta() {

        return ResourceManager.getShipParts().getMeta(getModuleId());
    }

    public ShipPartData getData() {

        return ResourceManager.getShipParts().findByPartId(getModuleId());
    }

    public static BattleFleetAttackModule getByPart(int guid, int id, int index, ShipPartData data) {

        BattleFleetAttackModule weapon = new BattleFleetAttackModule();
        PartLevelMeta level = data.getLevel(id);

        weapon.setReload(1);
        weapon.setModuleType(data.getPartType());
        weapon.setModuleSubType(data.getPartSubType());

        weapon.setFuelUsage(level.getFuelUsage());
        weapon.setModuleIndex(index);
        weapon.setModuleId(id);
        weapon.setGuid(guid);
        weapon.setLastShoot(-10000);

        weapon.setDamageType(level.getEffectString("damageType"));
        weapon.setCooldown((int) level.getEffectValue("reload"));

        weapon.setMinRange((int) level.getEffectMin("range"));
        weapon.setMaxRange((int) level.getEffectMax("range"));


        weapon.setMinAttack((int) level.getEffectMin("attack"));
        weapon.setMaxAttack((int) level.getEffectMax("attack"));

        weapon.setIntercept(level.getEffectValue("intercept"));
        weapon.setSteering(level.getEffectValue("steering"));
        weapon.setCritRate(level.getEffectValue("critRate"));
        weapon.setHitRate(level.getEffectValue("hitRate"));

        return weapon;

    }

    public int getMaxRange(ShipTechs techs) {

        switch (getModuleSubType()) {
            case "directional":
                return (int) (getMaxRange() + techs.getLaserIncreaseRangeAmount());
            case "ballistic":
                return (int) (getMaxRange() + techs.getBallisticIncreaseRange());
            case "shipBased":
                return (int) (getMaxRange() + techs.getShipBasedIncreaseRange());
            case "missile":
                return (int) (getMaxRange() + techs.getMissileIncreaseRange());
            default:
                return getMaxRange();
        }
    }

}

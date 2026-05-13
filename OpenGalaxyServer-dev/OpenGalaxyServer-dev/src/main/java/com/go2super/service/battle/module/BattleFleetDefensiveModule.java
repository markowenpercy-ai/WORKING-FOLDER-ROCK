package com.go2super.service.battle.module;

import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartBonusesMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.resources.data.meta.PartSpecialMeta;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class BattleFleetDefensiveModule extends ShipModule implements Serializable {

    private String partName;
    private double armor;
    private double shield;
    private double agility;
    private double daedalus;
    private double stability;

    private double interceptMissile;
    private double interceptShipBased;
    private double intercept;

    private double reflectRatioDamage;
    private double damageReflection;

    private double damageReduction;
    private double damageNegate;

    private double armorHeal;
    private double shieldHeal;

    private PartBonusesMeta bonusesMeta;
    private PartSpecialMeta[] specialMeta;

    private boolean postShield;
    private boolean broken;
    private boolean hide;

    public static BattleFleetDefensiveModule getByPart(int guid, int id, int index, ShipPartData data) {

        BattleFleetDefensiveModule module = new BattleFleetDefensiveModule();
        PartLevelMeta level = data.getLevel(id);

        module.setPartName(data.getName());
        module.setReload(1);
        module.setModuleType(data.getPartType());
        module.setModuleSubType(data.getPartSubType());

        module.setFuelUsage(level.getFuelUsage());
        module.setModuleIndex(index);
        module.setModuleId(id);
        module.setGuid(guid);
        module.setLastShoot(-10000);

        module.setArmor(level.getEffectValue("armor"));
        module.setShield(level.getEffectValue("shield"));
        module.setAgility(level.getEffectValue("agility"));
        module.setDaedalus(level.getEffectValue("daedalus"));
        module.setStability(level.getEffectValue("stability"));
        module.setCooldown((int) level.getEffectValue("reload"));

        module.setIntercept(level.getEffectValue("intercept"));
        module.setInterceptMissile(level.getEffectValue("interceptMissile"));
        module.setInterceptShipBased(level.getEffectValue("interceptShipBased"));

        module.setReflectRatioDamage(level.getEffectValue("reflectRatioDamage"));
        module.setDamageReflection(level.getEffectValue("damageReflection"));

        module.setDamageReduction(level.getEffectValue("damageReduction"));
        module.setDamageNegate(level.getEffectValue("damageNegate"));
        module.setSpecialMeta(level.getSpecialEffect("damageNegateSpecial"));

        module.setArmorHeal(level.getEffectValue("armorHeal"));
        module.setShieldHeal(level.getEffectValue("shieldHeal"));

        // todo others meta
        return module;

    }

    public boolean isShield() {

        return getModuleSubType().equals("shield");
    }

    public boolean isArmor() {

        return getModuleSubType().equals("armor");
    }


    public double getTotalNegation(List<BattleFleetAttackModule> addition) {

        return getDamageNegate() +
               getNegation(addition);
    }

    public double getNegation(List<BattleFleetAttackModule> type) {

        if (specialMeta == null || specialMeta.length == 0) {
            return 0;
        }
        for (PartSpecialMeta meta : specialMeta) {
            if (type.stream().anyMatch(x -> x.getDamageType() == meta.getType())) {
                return meta.getAmount();
            }
        }
        return 0;
    }

    public double getNegation(){
        return getDamageNegate();
    }

    public boolean isInterceptingMissiles() {

        return interceptMissile == 1;
    }

    public boolean isInterceptingShipBased() {

        return interceptShipBased == 1;
    }

}

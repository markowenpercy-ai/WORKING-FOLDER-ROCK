package com.go2super.service.battle.module;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartBonusesMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class BattleFleetAuxiliaryModule extends ShipModule implements Serializable {

    private double agility;
    private double daedalus;
    private double stability;

    private double steeringBonus;
    private double critRateBonus;
    private double hitRateBonus;
    private PartBonusesMeta bonusesMeta;

    public PartLevelMeta getMeta() {

        return ResourceManager.getShipParts().getMeta(getModuleId());
    }

    public ShipPartData getData() {

        return ResourceManager.getShipParts().findByPartId(getModuleId());
    }

    public static BattleFleetAuxiliaryModule getByPart(int guid, int id, int index, ShipPartData data) {

        BattleFleetAuxiliaryModule module = new BattleFleetAuxiliaryModule();
        PartLevelMeta level = data.getLevel(id);

        module.setReload(1);
        module.setModuleType(data.getPartType());
        module.setModuleSubType(data.getPartSubType());

        module.setModuleIndex(index);
        module.setModuleId(id);
        module.setGuid(guid);
        module.setBonusesMeta(level.getPartBonus("partBonus"));

        return module;

    }

    public double getDamageNegate(String partName) {
        if (getBonusesMeta() == null) {
            return 0;
        }
        return getBonusesMeta().getDouble(partName);
    }

}

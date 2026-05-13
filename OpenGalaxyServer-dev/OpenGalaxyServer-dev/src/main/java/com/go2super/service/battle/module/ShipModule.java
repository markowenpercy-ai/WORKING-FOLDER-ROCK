package com.go2super.service.battle.module;

import com.go2super.database.entity.ShipModel;
import com.go2super.logger.BotLogger;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartEffectMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.service.PacketService;
import com.go2super.service.battle.BattleFleetCell;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.*;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class ShipModule implements Serializable {

    private int guid = -1;
    private int lastShoot;

    private int cooldown; // Cooldown is the addition of reload each round that is used
    private int reload; // Reload is the round that fleet module can be used again
    private double fuelUsage; // Module HE3 cost when used

    private int moduleId;
    private int moduleIndex;

    private String moduleType;
    private String moduleSubType;

    private List<PartEffectMeta> effects;

    public PartLevelMeta getMeta() {

        return ResourceManager.getShipParts().getMeta(getModuleId());
    }

    public ShipPartData getData() {

        return ResourceManager.getShipParts().findByPartId(getModuleId());
    }

    public static List<ShipModule> getModules(int guid, BattleFleetCell cell) {

        List<ShipModule> modules = new ArrayList<>();
        int index = 0;

        ShipModel shipModel = PacketService.getShipModel(cell.getShipModelId());

        for (int part : shipModel.getParts()) {

            ShipPartData data = ResourceManager.getShipParts().findByPartId(part);

            switch (data.getPartType()) {
                case "attack":
                    BattleFleetAttackModule attackModule = BattleFleetAttackModule.getByPart(guid, part, index, data);
                    modules.add(attackModule);
                    if (data.getPartSubType().equals("shipBased")) {
                        BattleFleetDefensiveModule defensiveModule = BattleFleetDefensiveModule.getByPart(guid, part, ++index, data);
                        modules.add(defensiveModule);
                    }
                    break;
                case "defense":
                case "module":
                    BattleFleetDefensiveModule defensiveModule = BattleFleetDefensiveModule.getByPart(guid, part, index, data);
                    modules.add(defensiveModule);
                    break;
                case "passive":
                    BattleFleetAuxiliaryModule auxiliaryModule = BattleFleetAuxiliaryModule.getByPart(guid, part, index, data);
                    modules.add(auxiliaryModule);
                    break;
                default:
                    BotLogger.error("PartType not found: " + data.getPartType());
            }

            index++;

        }

        return modules;

    }

}

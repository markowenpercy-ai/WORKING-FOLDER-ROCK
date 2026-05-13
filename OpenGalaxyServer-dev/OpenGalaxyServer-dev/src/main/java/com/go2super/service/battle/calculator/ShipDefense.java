package com.go2super.service.battle.calculator;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipDefense {

    private List<ShipInterception> shipInterceptions = new ArrayList<>();
    private List<ShipFacility> shipFacilities = new ArrayList<>();

    private List<ShipMitigation> shieldMitigations = new ArrayList<>();
    private List<ShipMitigation> armorMitigations = new ArrayList<>();

    public void pass(List<BattleFleetDefensiveModule> defensiveModules, List<BattleFleetAttackModule> attackModules) {

        LinkedHashMap<Integer, List<BattleFleetDefensiveModule>> mappedModules = defensiveModules.stream()
            .collect(Collectors.groupingBy(module -> module.getModuleId(), LinkedHashMap::new, Collectors.toList()));
        String damageType = attackModules.stream().map(x -> x.getDamageType())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
                .max(Map.Entry.comparingByValue()).get().getKey();
        for (Map.Entry<Integer, List<BattleFleetDefensiveModule>> facilityModule : mappedModules.entrySet()) {

            ShipFacility shipFacility = new ShipFacility();

            for (BattleFleetDefensiveModule defensiveModule : facilityModule.getValue()) {
                shipFacility.add(defensiveModule, damageType);
            }

            getShipFacilities().add(shipFacility);

        }

    }

    public Optional<ShipFacility> getFacility(String name) {

        ShipPartData shipPartData = ResourceManager.getShipParts().findByPartName(name);
        if (shipPartData == null) {
            return Optional.empty();
        }
        for (PartLevelMeta levelMeta : shipPartData.getLevels()) {
            if (shipFacilities.stream().anyMatch(shipFacility -> shipFacility.getModuleId() == levelMeta.getId())) {
                return shipFacilities.stream().filter(shipFacility -> shipFacility.getModuleId() == levelMeta.getId()).findFirst();
            }
        }
        return Optional.empty();
    }

    public boolean hasFacility(String name) {

        ShipPartData shipPartData = ResourceManager.getShipParts().findByPartName(name);
        if (shipPartData == null) {
            return false;
        }
        for (PartLevelMeta levelMeta : shipPartData.getLevels()) {
            if (shipFacilities.stream().anyMatch(shipFacility -> shipFacility.getModuleId() == levelMeta.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean hasFacility(int groupId) {

        ShipPartData shipPartData = ResourceManager.getShipParts().findByGroupId(groupId);
        if (shipPartData == null) {
            return false;
        }
        for (PartLevelMeta levelMeta : shipPartData.getLevels()) {
            if (shipFacilities.stream().anyMatch(shipFacility -> shipFacility.getModuleId() == levelMeta.getId())) {
                return true;
            }
        }
        return false;
    }

}

package com.go2super.service.battle;

import com.go2super.database.entity.ShipModel;
import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.obj.game.ShipTeamNum;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.PartBonusMeta;
import com.go2super.resources.data.meta.PartEffectMeta;
import com.go2super.service.PacketService;
import com.go2super.service.battle.calculator.*;
import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.service.battle.module.BattleFleetAuxiliaryModule;
import com.go2super.service.battle.module.BattleFleetDefensiveModule;
import com.go2super.service.battle.module.ShipModule;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;
import java.util.stream.*;

@Data
public class BattleFleetCell implements Serializable {

    private int guid = -1;

    private int shipModelId;
    private int amount;

    private double usedHe3;
    private double storage;

    private int shields;
    private int structure;

    private int maxShields;
    private int maxStructure;

    private int movement;

    private List<ShipModule> modules = new ArrayList<>();
    private ShipEffects effects = new ShipEffects();

    public boolean isEmpty() {

        return shipModelId == -1 || amount == 0;
    }

    public void process(ShipEffect shipEffect) {

        Optional<ShipEffect> optionalEffect = effects.getEffects().stream().filter(effect -> effect.getEffectType() == shipEffect.getEffectType()).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();
            if (effect.getEffectType().isStackable()) {
                effect.setValue(effect.getValue() + shipEffect.getValue());
            } else {
                effect.setValue(shipEffect.getValue());
            }
            effect.setUntil(shipEffect.getUntil());

            if (shipEffect.getValue() == 0.0d) {
                effects.getEffects().remove(effect);
            }
            return;

        }

        if (shipEffect.getValue() == 0.0d) {
            return;
        }
        effects.getEffects().add(shipEffect);

    }

    public void doReduction(ShipReduction shipReduction, BattleFleet fleet) {

        shields -= shipReduction.getShieldsReduction();
        structure -= shipReduction.getStructureReduction();
        amount -= shipReduction.getAmountReduction();

        var deduct = Math.max(0, fleet.getHe3() - shipReduction.getSupplyReduction());
        fleet.setHe3(deduct);
        // Destroy
        if (amount <= 0) {
            this.shipModelId = -1;
        }

    }

    public ShipReduction makeReduction(double shieldDamage, double armorDamage, double supply, boolean maximumStability, ShipStats shipStats, BattleFleet fleet) {

        shieldDamage = shieldDamage < 0 ? 0 : shieldDamage;
        armorDamage = armorDamage < 0 ? 0 : armorDamage;
        supply = supply < 0 ? 0 : supply;

        ShipModel model = PacketService.getShipModel(shipModelId);
        double shieldReduction = 0, structureReduction = 0, amountReduction = 0, supplyReduction = supply;

        if (shields > 0) {
            if ((shields - shieldDamage) < 0) {
                shieldReduction = shields;
                shields = 0;
            } else {
                shieldReduction = shieldDamage;
                shields -= shieldDamage;
            }
        }

        if (structure > 0) {
            if ((structure - armorDamage) < 0) {
                structureReduction = structure;
                structure = 0;
            } else {
                structureReduction = armorDamage;
                structure -= armorDamage;
            }
        }

        if (!maximumStability) {

            double stability = shipStats.getStability();
            double unitStructure = model.getStructure();

            double oneShip = Math.max(unitStructure * stability * (1 + shipStats.getStabilityBonus()), 0.0d);
            double destroy = Math.floor(armorDamage / oneShip);

            if (structure <= 0 || destroy > this.amount) {
                amountReduction = this.amount;
                this.amount = 0;
            } else {
                amountReduction = destroy;
                this.amount -= destroy;
            }

        }

        // Reduce Supply
        double storageReduction = model.getFuelStorage() * amountReduction;
        fleet.setMaxHe3(fleet.getMaxHe3() - storageReduction);
        if(fleet.getMaxHe3() < fleet.getHe3()){
            //reduce only here
            if (storageReduction > usedHe3) {
                storageReduction -= usedHe3;
                usedHe3 = 0;
            } else {
                usedHe3 += storageReduction;
            }
            supplyReduction += storageReduction;
        }
        // Used HE3
        usedHe3 += supplyReduction;

        // Apply reduction
        if (supplyReduction > fleet.getHe3()) {
            supplyReduction = fleet.getHe3();
        }

        fleet.setHe3(fleet.getHe3() - supplyReduction);

        // Destroy the ship cell
        if (this.amount <= 0 || this.structure <= 0) {

            if (this.amount > 0) {
                amountReduction += this.amount;
            }
            if (this.structure > 0) {
                structureReduction += this.structure;
            }

            this.amount = 0;
            this.structure = 0;
            this.shipModelId = -1;
        }

        return ShipReduction.builder()
            .shieldsReduction(shieldReduction)
            .structureReduction(structureReduction)
            .supplyReduction(supplyReduction)
            .storageReduction(storageReduction)
            .amountReduction(amountReduction)
            .build();

    }

    public ShipReduction reflection(double generalDamage, double supply, BattleFleet fleet) {

        generalDamage = generalDamage < 0 ? 0 : generalDamage;
        supply = supply < 0 ? 0 : supply;

        ShipModel model = PacketService.getShipModel(shipModelId);

        double shieldReduction = 0, structureReduction = 0, amountReduction = 0, supplyReduction = supply;

        if (shields > 0) {
            if ((shields - generalDamage) < 0) {
                shieldReduction = shields;
                generalDamage -= shieldReduction;
                shields = 0;
            } else {
                shieldReduction = generalDamage;
                shields -= generalDamage;
                generalDamage = 0;
            }
        }

        if (structure > 0) {
            if ((structure - generalDamage) < 0) {
                structureReduction = structure;
                structure = 0;
            } else {
                structureReduction = generalDamage;
                structure -= generalDamage;
            }
        }

        double unitStructure = model.getStructure();

        double oneShip = (unitStructure);
        double destroy = Math.floor(structureReduction / oneShip);

        if (structure <= 0 || destroy > this.amount) {
            amountReduction = this.amount;
            this.amount = 0;
        } else {
            amountReduction = destroy;
            this.amount -= destroy;
        }

        // Reduce Supply
        double storageReduction = model.getFuelStorage() * amountReduction;
        fleet.setMaxHe3(fleet.getMaxHe3() - storageReduction);
        if(fleet.getMaxHe3() < fleet.getHe3()){
            //reduce only here
            if (storageReduction > usedHe3) {
                storageReduction -= usedHe3;
                usedHe3 = 0;
            } else {
                usedHe3 -= storageReduction;
                storageReduction = 0;
            }
            supplyReduction += storageReduction;
        }

        // Used HE3
        usedHe3 += supplyReduction;

        // Apply reduction
        if (supplyReduction > fleet.getHe3()) {
            supplyReduction = fleet.getHe3();
        }

        fleet.setHe3(fleet.getHe3() - supplyReduction);

        // Destroy the ship cell
        if (this.amount == 0) {
            this.shipModelId = -1;
        }

        return ShipReduction.builder()
                .shieldsReduction(Math.ceil(shieldReduction))
                .structureReduction(Math.ceil(structureReduction))
                .supplyReduction(supplyReduction)
                .storageReduction(storageReduction)
                .reflectionReduction(Math.ceil(shieldReduction) + Math.ceil(structureReduction))
                .amountReduction(amountReduction)
                .build();

    }

    public ShipReduction durability(double generalDamage) {

        generalDamage = Math.max(generalDamage, 0);
        double shieldReduction = 0, structureReduction = 0, amountReduction = 0, supplyReduction = 0;

        if (shields > 0) {
            if ((shields - generalDamage) < 0) {
                shieldReduction = shields;
                generalDamage -= shieldReduction;
                shields = 0;
            } else {
                shieldReduction = generalDamage;
                shields -= generalDamage;
                generalDamage = 0;
            }
        }

        if (structure > 0) {
            if ((structure - generalDamage) < 0) {
                structureReduction = structure;
                structure = 0;
            } else {
                structureReduction = generalDamage;
                structure -= generalDamage;
            }
        }

        if (structure <= 0) {
            structure = 1;
        }

        return ShipReduction.builder()
                .shieldsReduction(Math.ceil(shieldReduction))
                .structureReduction(Math.ceil(structureReduction))
                .supplyReduction(supplyReduction)
                .storageReduction(0)
                .reflectionReduction(Math.ceil(shieldReduction) + Math.ceil(structureReduction))
                .amountReduction(amountReduction)
                .build();

    }

    public ShipReduction shields(double generalDamage) {

        generalDamage = Math.max(generalDamage ,0);
        double shieldReduction = 0, structureReduction = 0;

        if (shields > 0) {
            var finalShield = shields - (int)Math.abs(Math.floor(generalDamage));
            if (finalShield <= 0) {
                shieldReduction = shields - 1;
                shields = 1;
            } else {
                shieldReduction = generalDamage;
                shields = finalShield;
            }

        }

        return ShipReduction.builder()
            .shieldsReduction(shieldReduction)
            .structureReduction(structureReduction)
            .supplyReduction(0)
            .storageReduction(0)
            .reflectionReduction(shieldReduction + structureReduction)
            .amountReduction(0)
            .build();

    }

    public ShipReduction structure(double generalDamage) {

        generalDamage = Math.max(generalDamage ,0);
        double shieldReduction = 0, structureReduction = 0;

        if (structure > 0) {
            var finalShield = structure - (int)Math.abs(Math.floor(generalDamage));
            if (finalShield <= 0) {
                structureReduction = structure - 1;
                structure = 1;
            } else {
                structureReduction = generalDamage;
                structure = finalShield;
            }
        }

        return ShipReduction.builder()
                .shieldsReduction(shieldReduction)
                .structureReduction(structureReduction)
                .supplyReduction(0)
                .storageReduction(0)
                .reflectionReduction(shieldReduction + structureReduction)
                .amountReduction(0)
                .build();

    }

    public ShipAttack getWeaponsToAttack(int roundId, int range, ShipTechs techs, boolean fort) {

        List<BattleFleetAttackModule> reloadedWeapons = getReloadedWeapons(roundId, range, techs, fort);
        List<BattleFleetAttackModule> toRemove = new ArrayList<>();

        String subType = null;
        String damageType = null;

        for (BattleFleetAttackModule weapon : reloadedWeapons) {
            if (subType == null) {
                subType = weapon.getData().getPartSubType();
            } else if (!subType.equals(weapon.getData().getPartSubType())) {
                toRemove.add(weapon);
            }
        }

        if (!reloadedWeapons.isEmpty()) {
            var optionalAttack = reloadedWeapons.stream().filter(x -> x.getDamageType() != null)
                    .map(BattleFleetAttackModule::getDamageType)
                    .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
                    .max(Map.Entry.comparingByValue());
            if(optionalAttack.isPresent()){
                damageType = optionalAttack.get().getKey();
            }
        }

        reloadedWeapons.removeAll(toRemove);
        return ShipAttack.builder()
            .attackModules(reloadedWeapons)
            .weaponSubType(subType)
            .damageType(damageType)
            .build();

    }

    public List<BattleFleetAttackModule> getReloadedWeapons(int roundId, int range, ShipTechs techs, boolean fort) {

        List<BattleFleetAttackModule> reloadedWeapons = new ArrayList<>();

        for (BattleFleetAttackModule weapon : getWeaponModules(fort).stream().filter(x -> x.getMinRange() <= range && x.getMaxRange(techs) >= range).toList()) {
            if (weapon.getLastShoot() + weapon.getCooldown() < roundId) {
                reloadedWeapons.add(weapon);
            }
        }

        return reloadedWeapons;

    }

    public BattleFleetAttackModule getWeaponById(int weaponId, boolean fort) {

        for (BattleFleetAttackModule weapon : getWeaponModules(fort)) {
            if (weapon.getModuleId() == weaponId) {
                return weapon;
            }
        }
        return null;
    }

    public BattleFleetDefensiveModule getDefenseByIndex(int defenseIndex) {

        for (BattleFleetDefensiveModule defense : getDefensiveModules()) {
            if (defense.getModuleIndex() == defenseIndex) {
                return defense;
            }
        }
        return null;
    }

    public BattleFleetAttackModule getWeaponByIndex(int weaponIndex, boolean fort) {

        for (BattleFleetAttackModule weapon : getWeaponModules(fort)) {
            if (weapon.getModuleIndex() == weaponIndex) {
                return weapon;
            }
        }
        return null;
    }

    public List<BattleFleetAttackModule> getWeaponModules(boolean fort) {

        List<BattleFleetAttackModule> fleetAttackModules = modules.stream()
            .filter(BattleFleetAttackModule.class::isInstance)
            .map(BattleFleetAttackModule.class::cast)
            .collect(Collectors.toList());
        if (!fort) {
            return fleetAttackModules.stream().filter(weapon -> !weapon.getModuleSubType().equals("building")).collect(Collectors.toList());
        }
        return fleetAttackModules;
    }

    public List<BattleFleetDefensiveModule> getDefensiveModules() {

        return modules.stream()
            .filter(BattleFleetDefensiveModule.class::isInstance)
            .map(BattleFleetDefensiveModule.class::cast)
            .collect(Collectors.toList());
    }

    public List<BattleFleetAuxiliaryModule> getAuxiliaryModules() {

        return modules.stream()
            .filter(BattleFleetAuxiliaryModule.class::isInstance)
            .map(BattleFleetAuxiliaryModule.class::cast)
            .collect(Collectors.toList());
    }

    public List<Pair<Integer, ShipPartData>> getAttackParts() {

        List<Pair<Integer, ShipPartData>> parts = new ArrayList<>();
        ShipModel model = PacketService.getShipModel(shipModelId);

        for (int part : model.getParts()) {

            ShipPartData data = ResourceManager.getShipParts().findByPartId(part);

            if (data.getPartType().equals("attack")) {
                parts.add(Pair.of(part, data));
            }

        }

        return parts;

    }

    public int getDurability() {

        return shields + structure;
    }

    public int getMaxDurability() {

        return maxShields + maxStructure;
    }

    public List<Pair<Integer, ShipPartData>> getPartsByType(String type) {

        List<Pair<Integer, ShipPartData>> parts = new ArrayList<>();
        ShipModel model = PacketService.getShipModel(shipModelId);

        for (int part : model.getParts()) {

            ShipPartData data = ResourceManager.getShipParts().findByPartId(part);

            if (data.getPartType().equals(type)) {
                parts.add(Pair.of(part, data));
            }

        }

        return parts;

    }

    public ShipModel getModel() {

        return PacketService.getShipModel(shipModelId);
    }

    public ShipTeamNum getTeamNum() {

        ShipTeamNum shipTeamNum = new ShipTeamNum();

        shipTeamNum.setNum(amount);
        shipTeamNum.setShipModelId(shipModelId);

        return shipTeamNum;

    }

    public boolean hasShips() {

        return shipModelId > -1 && amount > 0;
    }

    public static BattleFleetCell getByNum(int guid, ShipTeamNum num, BattleCommander commander, ShipTechs techs) {

        BattleFleetCell cell = new BattleFleetCell();

        cell.setGuid(guid);
        cell.setShipModelId(num.getShipModelId());
        cell.setAmount(num.getNum());

        if (cell.getAmount() > 0) {

            ShipModel model = PacketService.getShipModel(cell.getShipModelId());

            int modelStructure = (int) (model.getStructure() + commander.getAdditionalArmor());
            int modelShields = (int) (model.getShields() + commander.getAdditionalShield());

            double structureIncrement = commander.getStructureIncrement() + techs.getShipArmorBonus();
            double shieldIncrement = commander.getShieldIncrement() + techs.getShipShieldBonus();

            int initialStructure = (int) (modelStructure * (1 + structureIncrement));
            int initialShields = (int) (modelShields * (1 + shieldIncrement));

            cell.setStorage(model.getFuelStorage() * num.getNum());

            cell.setShields(initialShields * num.getNum());
            cell.setStructure(initialStructure * num.getNum());

            cell.setMaxShields(initialShields * num.getNum());
            cell.setMaxStructure(initialStructure * num.getNum());

            cell.setMovement(model.getMovement());
            cell.setModules(ShipModule.getModules(guid, cell));

        }

        return cell;

    }

}

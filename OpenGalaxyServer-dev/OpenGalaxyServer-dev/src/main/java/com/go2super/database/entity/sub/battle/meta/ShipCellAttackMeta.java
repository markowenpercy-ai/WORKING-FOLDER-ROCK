package com.go2super.database.entity.sub.battle.meta;

import com.go2super.obj.game.IntegerArray;
import com.go2super.service.battle.calculator.*;
import com.go2super.service.battle.type.EffectType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Transient;

import java.util.*;

@ToString
@Setter
@Getter
public class ShipCellAttackMeta {

    public int sourceReduceSupply;
    public int targetReduceSupply;

    public int sourceReduceStorage;
    public int targetReduceStorage;

    public int sourceReduceHp;
    public int sourceReduceShipNum;

    public int[] targetReduceShield = new int[9];
    public int[] targetReduceStructure = new int[9];

    public int[] targetReduceShipNum = new int[9];
    public int[] targetRepairShipNum = new int[9];

    public int[] sourcePartId = new int[7];
    public int[] sourcePartNum = new int[7];
    public int[] sourcePartRate = new int[7];

    public int[] targetPartId = new int[7];
    public int[] targetPartNum = new int[7];

    // Source commander skill usage animation
    public int sourceSkill;

    // Target commander skill usage animation
    public int targetSkill;

    // Source/Target shooting animations
    // 0 = Attacker normal hit
    // 1 = Attacker critical attack
    // 2 = Attacker normal hit and Defender critical
    // 3 = Attacker critical attack and Defender critical
    // 4 = Attacker double hits
    // 5 = Attacker double hits - critical attack
    // 6 = Attacker double hits and Defender critical
    // 7 = Attacker double hits - critical attack and Defender critical
    public int targetBlast;

    // For internal use purpose
    private int fromShipTeamId;
    private int toShipTeamId;

    private int fromUserGuid;
    private int toUserGuid;

    private int fromAmount;
    private int toAmount;

    private int fromId;
    private int toId;

    private boolean attack;

    // For matrix utility
    private int attackerDirection;
    private int attackerSegmentedPosIndex;
    private int attackerPosIndex;
    private int attackerPos;

    private int defenderDirection;
    private int defenderSegmentedPosIndex;
    private int defenderPosIndex;
    private int defenderPos;

    // For playback utility
    private ShipReduction reflectionReduction;

    private List<ShipReduction> shipReductions;
    private List<ShipShootdowns> shipShootdowns;
    private List<FortShootdowns> fortShootdowns;

    private ShipHighestAttack attackerHighestAttack;
    private ShipHighestAttack defenderHighestAttack;

    private List<ModuleUsage> defenderUsages;
    private List<ModuleUsage> attackerUsages;

    private Map<ShipPosition, ShipEffects> defenderEffects = new HashMap<>();
    private Map<ShipPosition, ShipEffects> attackerEffects = new HashMap<>();

    @Transient
    private List<ShipAttackPacketAction> shipAttackPacketActions = new ArrayList<>();

    public ShipCellAttackMeta() {

        Arrays.fill(targetReduceShield, 0);
        Arrays.fill(targetReduceStructure, -1);
        Arrays.fill(targetReduceShipNum, 0);
        Arrays.fill(targetRepairShipNum, 0);

        Arrays.fill(sourcePartId, -1);
        Arrays.fill(sourcePartNum, -1);
        Arrays.fill(sourcePartRate, -1);

        Arrays.fill(targetPartId, -1);
        Arrays.fill(targetPartNum, -1);

    }

    public void add(ShipAttackPacketAction packetAction) {

        getShipAttackPacketActions().add(packetAction);
    }

    public IntegerArray targetReduceShieldBuffer() {

        return new IntegerArray(targetReduceShield);
    }

    public IntegerArray targetReduceStructureBuffer() {

        return new IntegerArray(targetReduceStructure);
    }

    public IntegerArray targetReduceShipNumBuffer() {

        return new IntegerArray(targetReduceShipNum);
    }

    public IntegerArray targetRepairShipNumBuffer() {

        return new IntegerArray(targetRepairShipNum);
    }

    public IntegerArray sourcePartIdBuffer() {

        return new IntegerArray(sourcePartId);
    }

    public IntegerArray sourcePartNumBuffer() {

        return new IntegerArray(sourcePartNum);
    }

    public IntegerArray sourcePartRateBuffer() {

        return new IntegerArray(sourcePartRate);
    }

    public IntegerArray targetPartIdBuffer() {

        return new IntegerArray(targetPartId);
    }

    public IntegerArray targetPartNumBuffer() {

        return new IntegerArray(targetPartNum);
    }

    public void setDefenderEffect(ShipPosition shipPosition, EffectType effectType, double value, int until) {

        if (!defenderEffects.containsKey(shipPosition)) {
            defenderEffects.put(shipPosition, ShipEffects.builder().effects(new ArrayList<>()).build());
        }

        ShipEffect shipEffect = ShipEffect.builder()
            .effectType(effectType)
            .value(value)
            .until(until)
            .build();

        shipPosition.getBattleFleetCell().process(shipEffect);
        Optional<ShipEffect> optionalEffect = defenderEffects.get(shipPosition).getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();

            effect.setValue(value);
            effect.setUntil(until);

        } else {

            defenderEffects.get(shipPosition).getEffects().add(shipEffect);

        }

    }

    public ShipEffect fetchDefenderAndRemove(ShipPosition shipPosition, EffectType effectType) {

        List<ShipEffect> shipEffects = getDefenderFleetEffects(shipPosition);

        Optional<ShipEffect> optionalResult = shipEffects.stream().filter(shipEffect -> shipEffect.getEffectType() == effectType).findFirst();
        if (optionalResult.isEmpty()) {
            return null;
        }

        shipEffects.remove(optionalResult.get());
        return optionalResult.get();

    }

    public void removeDefenderShipEffects(ShipPosition shipPosition, ShipEffect... shipEffects) {

        if (!defenderEffects.containsKey(shipPosition)) {
            return;
        }
        for (ShipEffect effect : shipEffects) {
            defenderEffects.get(shipPosition).getEffects().remove(effect);
        }
    }

    public List<ShipEffect> getDefenderFleetEffects(ShipPosition shipPosition) {

        if (!defenderEffects.containsKey(shipPosition)) {
            return new ArrayList<>();
        }
        return defenderEffects.get(shipPosition).getEffects();
    }

    public void removeAttackerEffect(ShipPosition shipPosition, EffectType effectType) {

        if (!attackerEffects.containsKey(shipPosition)) {
            attackerEffects.put(shipPosition, ShipEffects.builder().effects(new ArrayList<>()).build());
        }

        Optional<ShipEffect> optionalEffect = attackerEffects.get(shipPosition).getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();

            effect.setRemove(true);
            effect.setUntil(-1);

        } else {

            attackerEffects.get(shipPosition).getEffects().add(ShipEffect.builder()
                .effectType(effectType)
                .remove(true)
                .until(-1).build());

        }

    }

    public void removeDefenderEffect(ShipPosition shipPosition, EffectType effectType) {

        if (!defenderEffects.containsKey(shipPosition)) {
            defenderEffects.put(shipPosition, ShipEffects.builder().effects(new ArrayList<>()).build());
        }

        Optional<ShipEffect> optionalEffect = defenderEffects.get(shipPosition).getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();

            effect.setRemove(true);
            effect.setUntil(-1);

        } else {

            defenderEffects.get(shipPosition).getEffects().add(ShipEffect.builder()
                .effectType(effectType)
                .remove(true)
                .until(-1).build());

        }

    }

    public void addDefenderEffect(ShipPosition shipPosition, EffectType effectType, double value, int until) {

        if (!defenderEffects.containsKey(shipPosition)) {
            defenderEffects.put(shipPosition, ShipEffects.builder().effects(new ArrayList<>()).build());
        }

        ShipEffect shipEffect = ShipEffect.builder()
            .effectType(effectType)
            .value(value)
            .until(until)
            .build();

        shipPosition.getBattleFleetCell().process(shipEffect);
        Optional<ShipEffect> optionalEffect = defenderEffects.get(shipPosition).getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();

            effect.setValue(value + effect.getValue());
            effect.setUntil(until);

        } else {

            defenderEffects.get(shipPosition).getEffects().add(shipEffect);

        }

    }

    public void addAttackerEffect(ShipPosition shipPosition, EffectType effectType, double value, int until) {

        if (!attackerEffects.containsKey(shipPosition)) {
            attackerEffects.put(shipPosition, ShipEffects.builder().effects(new ArrayList<>()).build());
        }

        ShipEffect shipEffect = ShipEffect.builder()
            .effectType(effectType)
            .value(value)
            .until(until)
            .build();

        shipPosition.getBattleFleetCell().process(shipEffect);
        Optional<ShipEffect> optionalEffect = attackerEffects.get(shipPosition).getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();

            effect.setValue(value + effect.getValue());
            effect.setUntil(until);

        } else {

            attackerEffects.get(shipPosition).getEffects().add(shipEffect);

        }

    }

    public void setAttackerEffect(ShipPosition shipPosition, EffectType effectType, double value, int until) {

        if (!attackerEffects.containsKey(shipPosition)) {
            attackerEffects.put(shipPosition, ShipEffects.builder().effects(new ArrayList<>()).build());
        }

        ShipEffect shipEffect = ShipEffect.builder()
            .effectType(effectType)
            .value(value)
            .until(until)
            .build();

        shipPosition.getBattleFleetCell().process(shipEffect);
        Optional<ShipEffect> optionalEffect = attackerEffects.get(shipPosition).getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

        if (optionalEffect.isPresent()) {

            ShipEffect effect = optionalEffect.get();

            effect.setValue(value);
            effect.setUntil(until);

        } else {

            attackerEffects.get(shipPosition).getEffects().add(shipEffect);

        }

    }

    public ShipEffect fetchAttackerAndRemove(ShipPosition shipPosition, EffectType effectType) {

        List<ShipEffect> shipEffects = getAttackerFleetEffects(shipPosition);

        Optional<ShipEffect> optionalResult = shipEffects.stream().filter(shipEffect -> shipEffect.getEffectType() == effectType).findFirst();
        if (optionalResult.isEmpty()) {
            return null;
        }

        shipEffects.remove(optionalResult.get());
        return optionalResult.get();

    }

    public void removeAttackerShipEffects(ShipPosition shipPosition, ShipEffect... shipEffects) {

        for (ShipEffect effect : shipEffects) {
            attackerEffects.get(shipPosition).getEffects().remove(effect);
        }
    }

    public List<ShipEffect> getAttackerFleetEffects(ShipPosition shipPosition) {

        return attackerEffects.get(shipPosition).getEffects();
    }

}

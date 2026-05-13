package com.go2super.database.entity.sub.battle.trigger;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.battle.BattleActionTrigger;
import com.go2super.database.entity.sub.battle.meta.ShipCellAttackMeta;
import com.go2super.service.battle.calculator.FleetEffect;
import com.go2super.service.battle.calculator.FleetEffects;
import com.go2super.service.battle.calculator.ShipAttackPacketAction;
import com.go2super.service.battle.type.EffectType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Transient;

import java.util.*;

@Getter
@Setter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@ToString(callSuper = true)
public class FleetAttackFleetTrigger extends BattleActionTrigger {

    private int attackerShipTeamId;
    private int defenderShipTeamId;

    private int attackerDirection;
    private int defensiveDirection;

    private List<ShipCellAttackMeta> attacks;
    private int[] attackMatrix = new int[9];

    private List<FleetEffects> fleetEffects = new ArrayList<>();

    private int repelSteps;

    @Transient
    private List<ShipAttackPacketAction> shipAttackPacketActions = new ArrayList<>();

    public ShipCellAttackMeta getMeta(int fromId) {

        for (ShipCellAttackMeta attackMeta : attacks) {
            if (attackMeta.getFromId() == fromId) {
                return attackMeta;
            }
        }

        return null;

    }

    public void addEffect(BattleFleet battleFleet, EffectType effectType, double value, int until, boolean process) {

        if (process) {
            battleFleet.process(FleetEffect.builder().effectType(effectType).value(value).until(until).build());
        }
        addEffect(battleFleet.getShipTeamId(), effectType, value, until);
    }

    public void addEffect(BattleFleet battleFleet, EffectType effectType, double value, int until) {

        battleFleet.process(FleetEffect.builder().effectType(effectType).value(value).until(until).build());
        addEffect(battleFleet.getShipTeamId(), effectType, value, until);
    }

    public void setEffect(BattleFleet battleFleet, EffectType effectType, double value, int until) {

        battleFleet.process(FleetEffect.builder().effectType(effectType).value(value).until(until).build());
        setEffect(battleFleet.getShipTeamId(), effectType, value, until);
    }

    public void addEffect(int shipTeamId, EffectType effectType, double value, int until) {

        Optional<FleetEffects> optionalEffects = fleetEffects.stream().filter(current -> current.getShipTeamId() == shipTeamId).findFirst();

        if (optionalEffects.isPresent()) {

            FleetEffects effects = optionalEffects.get();
            Optional<FleetEffect> optionalEffect = effects.getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

            if (optionalEffect.isPresent()) {

                FleetEffect effect = optionalEffect.get();

                effect.setValue(value + effect.getValue());
                effect.setUntil(until);

            } else {

                effects.getEffects().add(FleetEffect.builder()
                    .effectType(effectType)
                    .value(value)
                    .until(until)
                    .build());

            }

        } else {

            FleetEffects effects = FleetEffects.builder()
                .shipTeamId(shipTeamId)
                .effects(new ArrayList<>())
                .build();

            effects.getEffects().add(FleetEffect.builder()
                .effectType(effectType)
                .value(value)
                .until(until)
                .build());

            fleetEffects.add(effects);

        }

    }

    public void setEffect(int shipTeamId, EffectType effectType, double value, int until) {

        Optional<FleetEffects> optionalEffects = fleetEffects.stream().filter(current -> current.getShipTeamId() == shipTeamId).findFirst();

        if (optionalEffects.isPresent()) {

            FleetEffects effects = optionalEffects.get();
            Optional<FleetEffect> optionalEffect = effects.getEffects().stream().filter(current -> current.getEffectType() == effectType).findFirst();

            if (optionalEffect.isPresent()) {

                FleetEffect effect = optionalEffect.get();

                effect.setValue(value);
                effect.setUntil(until);

            } else {

                effects.getEffects().add(FleetEffect.builder()
                    .effectType(effectType)
                    .value(value)
                    .until(until)
                    .build());

            }

        } else {

            FleetEffects effects = FleetEffects.builder()
                .shipTeamId(shipTeamId)
                .effects(new ArrayList<>())
                .build();

            effects.getEffects().add(FleetEffect.builder()
                .effectType(effectType)
                .value(value)
                .until(until)
                .build());

            fleetEffects.add(effects);

        }

    }

    public FleetEffect fetchAndRemove(EffectType effectType, BattleFleet battleFleet) {

        return fetchAndRemove(effectType, battleFleet.getShipTeamId());
    }

    public FleetEffect fetchAndRemove(EffectType effectType, int shipTeamId) {

        List<FleetEffect> fleetEffects = getFleetEffects(shipTeamId);

        Optional<FleetEffect> optionalResult = fleetEffects.stream().filter(fleetEffect -> fleetEffect.getEffectType() == effectType).findFirst();
        if (optionalResult.isEmpty()) {
            return null;
        }

        fleetEffects.remove(optionalResult.get());
        return optionalResult.get();

    }

    public void removeFleetEffects(int shipTeamId, FleetEffect... effects) {

        for (FleetEffects current : fleetEffects) {
            for (FleetEffect effect : effects) {
                if (current.getShipTeamId() == shipTeamId) {
                    current.getEffects().remove(effect);
                }
            }
        }
    }

    public void removeFleetEffects(int shipTeamId) {

        fleetEffects.stream().filter(fleetEffect -> fleetEffect.getShipTeamId() == shipTeamId).forEach(fleetEffect -> fleetEffects.remove(fleetEffect));
    }

    public List<FleetEffect> getFleetEffects(int shipTeamId) {

        List<FleetEffect> fleetEffects = new ArrayList<>();
        for (FleetEffects fleetEffect : getFleetEffects()) {
            if (fleetEffect.getShipTeamId() == shipTeamId) {
                fleetEffects.addAll(fleetEffect.getEffects());
            }
        }
        return fleetEffects;
    }

}

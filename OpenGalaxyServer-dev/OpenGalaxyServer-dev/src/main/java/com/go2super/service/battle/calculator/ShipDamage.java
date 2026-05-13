package com.go2super.service.battle.calculator;

import com.go2super.service.battle.module.BattleFleetAttackModule;
import com.go2super.socket.util.RandomUtil;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipDamage {

    private List<BattleFleetAttackModule> attackModules = new ArrayList<>();

    private double hitChance;
    private double hits;

    private double minDamage;
    private double maxDamage;
    public int getStacks(){
        return attackModules.size();
    }
    public String getDamageType(){
        var damageType = "fort";
        var optionalAttack = attackModules.stream().map(x -> x.getDamageType())
                .collect(Collectors.groupingBy(s -> s, Collectors.counting())).entrySet().stream()
                .max(Map.Entry.comparingByValue());
        if(optionalAttack.isPresent()){
            damageType = optionalAttack.get().getKey();
        }
        return damageType;
    }

    public  String getAttackType(){
        return  attackModules.get(0).getModuleSubType();
    }

    public void shoot(int round) {

        for (BattleFleetAttackModule module : attackModules) {
            module.setLastShoot(round);
        }
    }

    public void addUsage(ShipUsage usage, double hitChance) {

        for (BattleFleetAttackModule module : attackModules) {
            usage.add(module, hitChance, 0);
        }
    }

    public double getFuelUsage(int effectiveStack) {

        return (getReference().getFuelUsage() * (double) attackModules.size()) * (double) effectiveStack;
    }

    public double getSteering() {

        return getReference().getSteering();
    }

    public void calculate(int effectiveStack) {

        hitChance = getReference().getHitRate();
        hits = getStacks() * effectiveStack;
    }

    public BattleFleetAttackModule getReference() {

        return attackModules.iterator().next();
    }

    public void add(BattleFleetAttackModule attackModule) {

        minDamage += attackModule.getMinRange();
        maxDamage += attackModule.getMaxRange();

        attackModules.add(attackModule);

    }

}

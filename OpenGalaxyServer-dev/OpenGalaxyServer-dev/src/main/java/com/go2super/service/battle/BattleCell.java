package com.go2super.service.battle;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.database.entity.sub.BattleFort;
import com.go2super.service.battle.type.TargetInterval;
import lombok.Data;
import lombok.ToString;

import java.util.*;
import java.util.stream.*;

@Data
@ToString
public class BattleCell {

    private LinkedList<BattleFleet> attackers = new LinkedList<>();
    private LinkedList<BattleFleet> defenders = new LinkedList<>();
    private LinkedList<BattleFort> forts = new LinkedList<>();

    private int attackerStars = -1;
    private long maxAttackerAttack = -1;
    private long minAttackerAttack = -1;
    private long maxAttackerDurability = -1;
    private long minAttackerDurability = -1;

    private int defenderStars = -1;
    private long maxDefenderAttack = -1;
    private long minDefenderAttack = -1;
    private long maxDefenderDurability = -1;
    private long minDefenderDurability = -1;

    private int x = -1;
    private int y = -1;

    public void addFort(BattleFort fort) {

        if (x == -1) {
            x = fort.getPosX();
        }

        if (y == -1) {
            y = fort.getPosY();
        }

        forts.add(fort);

    }

    public void addFleet(BattleFleet fleet) {

        fleet.calculate();

        long attack = fleet.calculateAttack();
        long durability = fleet.getRoundDurability();
        int commander = fleet.getBattleCommander().getStars();

        if (x == -1) {
            x = fleet.getPosX();
        }

        if (y == -1) {
            y = fleet.getPosY();
        }

        if (fleet.isAttacker()) {

            if (attackerStars < commander) {
                attackerStars = commander;
            }

            if (maxAttackerAttack < attack || minAttackerAttack == -1) {
                maxAttackerAttack = attack;
            }

            if (minAttackerAttack > attack || minAttackerAttack == -1) {
                minAttackerAttack = attack;
            }

            if (maxAttackerDurability < durability || maxAttackerDurability == -1) {
                maxAttackerDurability = durability;
            }

            if (minAttackerDurability > durability || minAttackerDurability == -1) {
                minAttackerDurability = durability;
            }

            attackers.add(fleet);
            return;

        }

        if (defenderStars < commander) {
            defenderStars = commander;
        }

        if (maxDefenderAttack < attack || minDefenderAttack == -1) {
            maxDefenderAttack = attack;
        }

        if (minDefenderAttack > attack || minDefenderAttack == -1) {
            minDefenderAttack = attack;
        }

        if (maxDefenderDurability < durability || maxDefenderDurability == -1) {
            maxDefenderDurability = durability;
        }

        if (minDefenderDurability > durability || minDefenderDurability == -1) {
            minDefenderDurability = durability;
        }

        defenders.add(fleet);

    }


    public List<BattleFleet> getEnemies(boolean attacker) {

        return attacker ? getDefenders() : getAttackers();
    }

    public boolean isEmpty() {

        return attackers.isEmpty() && defenders.isEmpty();
    }

    public boolean hasEnemies(BattleFleet compare) {

        List<BattleFleet> enemies = getEnemies(compare.isAttacker());

        for (BattleFleet enemy : enemies) {
            if (enemy.isDestroyed()) {
                continue;
            } else if (enemy.isAttacker() && !compare.isAttacker()) {
                return true;
            } else if (enemy.isDefender() && !compare.isDefender()) {
                return true;
            }
        }

        for (BattleFort fort : forts) {
            if (fort.isDestroyed()) {
                continue;
            } else if (fort.isAttacker() && !compare.isAttacker()) {
                return true;
            } else if (fort.isDefender() && !compare.isDefender()) {
                return true;
            }
        }

        return false;

    }


}

package com.go2super.database.entity.sub;

import com.go2super.obj.game.FightRobResource;
import com.go2super.service.battle.BattleFleetCell;
import com.go2super.service.battle.Match;
import com.go2super.service.battle.calculator.FortShootdowns;
import com.go2super.service.battle.calculator.ShipHighestAttack;
import com.go2super.service.battle.calculator.ShipShootdowns;
import lombok.Data;

import java.util.*;

@Data
public class BattleReport {

    private int lastShoot = -1;
    private List<FightRobResource> fightRobResources = new ArrayList<>();

    private int totalAttackerSent = 0;
    private int totalDefenderSent = 0;

    private int totalAttackerLost = 0;
    private int totalDefenderLost = 0;

    private List<BattleExpCache> expHistoric = new ArrayList<>();
    private List<BattleShipCache> shipHistoric = new ArrayList<>();

    public void start(Match match) {

        for (BattleFort fort : match.getForts()) {

            if (fort.isAttacker()) {
                addAttackerSent(1);
            } else {
                addDefenderSent(1);
            }

        }

        for (BattleFleet fleet : match.getFleets()) {

            if (fleet.isAttacker()) {
                addAttackerSent(fleet.getAmount());
            } else {
                addDefenderSent(fleet.getAmount());
            }

        }

    }

    public void addAttackerSent(int amount) {

        totalAttackerSent += amount;
    }

    public void addDefenderSent(int amount) {

        totalDefenderSent += amount;
    }

    public void processHighestAttack(ShipHighestAttack shipHighestAttack) {

        processHighestAttack(shipHighestAttack.getHighestAttack(), shipHighestAttack.getAttacker(), shipHighestAttack.getAttackerCell());
    }

    public void processHighestAttack(double totalAttack, BattleFleet battleFleet, BattleFleetCell cell) {

        processHighestAttack(totalAttack, battleFleet.getGuid(), cell.getShipModelId());
    }

    public void processHighestAttack(double highestAttack, int guid, int shipModelId) {

        BattleShipCache battleShipCache = new BattleShipCache();
        battleShipCache.setGuid(guid);
        battleShipCache.setShipModelId(shipModelId);
        battleShipCache.setHighestAttack(highestAttack);

        processHighestAttack(battleShipCache, shipHistoric);

    }

    public void processHighestAttack(BattleShipCache shipCache, List<BattleShipCache> shipCacheList) {

        Optional<BattleShipCache> optional = shipCacheList.stream()
            .filter(cache -> cache.getGuid() == shipCache.getGuid() && cache.getShipModelId() == shipCache.getShipModelId())
            .findFirst();

        if (optional.isPresent()) {

            BattleShipCache current = optional.get();

            if (current.getHighestAttack() < shipCache.getHighestAttack()) {
                current.setHighestAttack(shipCache.getHighestAttack());
            }

        } else {
            shipCacheList.add(shipCache);
        }

    }

    public void processShootdowns(FortShootdowns fortShootdowns) {

        processShootdowns(fortShootdowns.getAmount(), fortShootdowns.isAttacker());
    }

    public void processShootdowns(ShipShootdowns shipShootdowns) {

        processShootdowns(shipShootdowns.getAmount(), shipShootdowns.getAttacker(), shipShootdowns.getAttackerCell());
    }

    public void processShootdowns(double amount, boolean attacker) {

        if (!attacker) {
            totalDefenderLost += amount;
        } else {
            totalAttackerLost += amount;
        }

    }

    public void processShootdowns(double amount, BattleFleet battleFleet, BattleFleetCell cell) {

        if (battleFleet.isAttacker()) {
            totalDefenderLost += amount;
        } else {
            totalAttackerLost += amount;
        }

        BattleShipCache battleShipCache = new BattleShipCache();
        battleShipCache.setGuid(battleFleet.getGuid());
        battleShipCache.setShipModelId(cell.getShipModelId());
        battleShipCache.setShootdowns(amount);

        processShootdowns(battleShipCache, shipHistoric);

    }

    public void processShootdowns(BattleShipCache shipCache, List<BattleShipCache> shipCacheList) {

        Optional<BattleShipCache> optional = shipCacheList.stream()
            .filter(cache -> cache.getGuid() == shipCache.getGuid() && cache.getShipModelId() == shipCache.getShipModelId())
            .findFirst();

        if (optional.isPresent()) {

            BattleShipCache current = optional.get();
            current.setShootdowns(current.getShootdowns() + shipCache.getShootdowns());

        } else {
            shipCacheList.add(shipCache);
        }

    }


    public String battleReportString() {
        return "Attacker Ships sent: " + totalAttackerSent + "\n" +
                "Attacker ships lost: " + totalAttackerLost + "\n" +
                "Defender Ships sent: " + totalDefenderSent + "\n" +
                "Defender ships lost: " + totalDefenderLost + "\n";
    }


}

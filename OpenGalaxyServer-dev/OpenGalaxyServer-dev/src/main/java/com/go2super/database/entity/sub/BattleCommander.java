package com.go2super.database.entity.sub;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.ShipModel;
import com.go2super.logger.BotLogger;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderPullulateData;
import com.go2super.resources.data.CommanderStatsData;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.service.CommanderService;
import com.go2super.service.PacketService;
import com.go2super.service.battle.BattleFleetCell;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.*;

@Data
public class BattleCommander implements Serializable {

    private int commanderId;
    private int skillId;

    private String name;
    private String nameId;

    private int stars;
    private int level;

    private double totalAccuracy;
    private double totalDodge;
    private double totalSpeed;
    private double totalElectron;

    private double temporalAccuracy;
    private double temporalDodge;
    private double temporalSpeed;
    private double temporalElectron;
    private double temporalDefenseRate;

    private double structureIncrement;
    private double shieldIncrement;
    private double attackPowerIncrement;
    private double criticalAttackDamageIncrement;
    private double criticalAttackRateIncrement;
    private double doubleAttackRateIncrement;
    private double shieldHealIncrement;
    private double experienceRateIncrement;

    private double additionalArmor;
    private double additionalShield;
    private double additionalDaedalus;
    private double additionalStability;
    private double additionalAbsorption;
    private double additionalArmorRegeneration;
    private double additionalShieldRegeneration;

    private List<Pair<String, Double>> additionalMinAttack = new ArrayList<>();
    private List<Pair<String, Double>> additionalMaxAttack = new ArrayList<>();

    private CommanderTrigger trigger;
    private CommanderExpertise expertise;

    public boolean isTotalMoreThan(BattleCommander commander) {

        double total = this.getTotalAccuracy() + this.getTotalDodge() + this.getTotalSpeed() + this.getTotalElectron();
        double otherTotal = commander.getTotalAccuracy() + commander.getTotalDodge() + commander.getTotalSpeed() + commander.getTotalElectron();
        return total > otherTotal;
    }

    public double getAdditionalMinAttack(String attack) {

        for (Pair<String, Double> pair : additionalMinAttack) {
            if (pair.getLeft().equals(attack)) {
                return pair.getRight();
            }
        }

        return 0.0d;

    }

    public double getAdditionalMaxAttack(String attack) {

        for (Pair<String, Double> pair : additionalMaxAttack) {
            if (pair.getLeft().equals(attack)) {
                return pair.getRight();
            }
        }

        return 0.0d;

    }

    public int getEffectiveStack(BattleFleetCell commandingCell) {

        int result = 0;

        if (!commandingCell.hasShips()) {
            return result;
        }

        ShipModel shipModel = PacketService.getShipModel(commandingCell.getShipModelId());
        ShipBodyData bodyData = ResourceManager.getShipBodies().findByBodyId(shipModel.getBodyId());
        String bodyType = bodyData.getBodyType();

        CommanderStatsData statsData = CommanderService.getInstance().getStats(skillId);
        CommanderPullulateData pullulateData = new CommanderPullulateData();

        if (statsData != null) {
            pullulateData = ResourceManager.getCommanders().getPullulate(stars - 1, statsData.typeCode());
        }

        int stackLimit = 0;

        switch (bodyType) {

            case "frigate":
                stackLimit = 1100 + pullulateData.getFrigate();
                break;

            case "cruiser":

                stackLimit = 1000 + pullulateData.getCruiser();
                break;

            case "battleship":
                stackLimit = 900 + pullulateData.getBattleship();
                break;

            case "flagship": // todo based on the wiki, need to be cruiser flagship and battleship flagship
                stackLimit = 1000 + pullulateData.getBattleship();
                break;

            default:

                BotLogger.error("BodyType: " + bodyType + " not identified!");
                stackLimit = 900;
                break;

        }

        if (commandingCell.getAmount() >= stackLimit) {
            result = stackLimit;
        }

        if (commandingCell.getAmount() < stackLimit) {
            result = commandingCell.getAmount();
        }

        return result;

    }

    public void addTemporalDefenseRate(double temporalDefenseRate) {

        this.temporalDefenseRate += temporalDefenseRate;
    }

    public void addTemporalAccuracy(double temporalAccuracy) {

        this.temporalAccuracy += temporalAccuracy;
    }

    public void addTemporalDodge(double temporalDodge) {

        this.temporalDodge += temporalDodge;
    }

    public void addTemporalElectron(double temporalElectron) {

        this.temporalElectron += temporalElectron;
    }

    public void addTemporalSpeed(double temporalSpeed) {

        this.temporalSpeed += temporalSpeed;
    }

    public double getTotalDodge() {

        return Math.max(temporalDodge + totalDodge, 0.0d);
    }

    public double getTotalElectron() {

        return Math.max(temporalElectron + totalElectron, 0.0d);
    }

    public double getTotalAccuracy() {

        return Math.max(temporalAccuracy + totalAccuracy, 0.0d);
    }

    public double getTotalSpeed() {

        return Math.max(temporalSpeed + totalSpeed, 0.0d);
    }

    public CommanderStatsData getStatsData() {

        return ResourceManager.getCommanders().getCommander(nameId);
    }

    public Commander getCommander() {

        return CommanderService.getInstance().getCommander(commanderId);
    }


    public List<Pair<String, Double>> getAdditionalMaxAttackWithoutPlanetary() {
        List<Pair<String, Double>> result = new ArrayList<>();
        for (Pair<String, Double> pair : additionalMaxAttack) {
            if (!pair.getLeft().equals("building")) {
                result.add(pair);
            }
        }

        return result;
    }

}

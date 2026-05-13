package com.go2super.database.entity.sub;

import com.go2super.database.entity.type.BattleElementType;
import com.go2super.database.entity.type.SpaceFortAttackType;
import com.go2super.database.entity.type.SpaceFortType;
import com.go2super.logger.BotLogger;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.meta.FortificationEffectMeta;
import com.go2super.resources.data.meta.FortificationLevelMeta;
import com.go2super.service.battle.astar.Node;
import com.go2super.service.battle.calculator.FortReduction;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

@Data
public class BattleFort extends BattleElement implements Serializable {

    private SpaceFortType fortType;
    private boolean defender;

    private int fortId;
    private int levelId;

    private int maxHealth;
    private int health;
    private int damage;

    private int targetShipTeamId = -1;

    private int radius;
    private int posX;
    private int posY;

    private int guid;
    private long userId;
    private double nextAttack;

    public BattleFort(SpaceFortType type, int levelId) {

        super(BattleElementType.FORTIFICATION);

        this.levelId = levelId;
        this.fortType = type;

        this.radius = type == SpaceFortType.COMMON_SPACE_STATION ? 2 : 1;

        FortificationLevelMeta meta = getMeta();
        if (meta == null) {
            BotLogger.error("FortificationLevelMeta not found for fortType: " + type + " and levelId: " + levelId);
            return;
        }

        double endure = type == SpaceFortType.RBP_SPACE_STATION ? 300 : 1;

        Optional<FortificationEffectMeta> optionalAssault = meta.getEffect("assault");
        if (optionalAssault.isPresent()) {
            this.damage = (int) optionalAssault.get().getValue();
        }

        Optional<FortificationEffectMeta> optionalEndure = meta.getEffect("endure");
        if (optionalEndure.isPresent()) {
            endure = optionalEndure.get().getValue();
        }

        this.maxHealth = (int) endure;
        this.health = (int) endure;

    }

    public void doReduction(FortReduction fortReduction) {

        health = (int) Math.max(health - fortReduction.getHealthReduction(), 0);

    }

    public FortReduction makeReduction(double damage) {

        double baseHealth = health;
        health = (int) Math.max(health - damage, 0);

        int healthReduction = (int) (health <= 0 ? baseHealth : baseHealth - health);

        return FortReduction.builder()
            .healthReduction(healthReduction)
            .build();

    }

    public FortificationLevelMeta getMeta() {

        return getFortType().getData(levelId);
    }

    public int getRange() {

        if (!hasCannon()) {
            return 0;
        }
        if (isStation()) {
            return 100;
        }

        if (fortType.isCommon()) {

            FortificationLevelMeta meta = getLevelMeta();
            return meta.getEffectValue("range");

        }

        FortificationLevelMeta meta = ResourceManager.getRBPFortification().getLevelMeta(fortType.getDataId(), levelId);
        return meta.getEffectValue("range");

    }

    public SpaceFortAttackType getAttackType() {

        if (!hasCannon()) {
            return SpaceFortAttackType.NONE;
        }
        return getLevelMeta().getAttackType();
    }

    public FortificationLevelMeta getLevelMeta() {

        return ResourceManager.getFortification().getLevelMeta(fortType.getDataId(), levelId);
    }

    public int getBuildType() {

        return getFortType().getBuildType();
    }

    public boolean isAttacker() {

        return !defender;
    }

    public boolean hasCannon() {

        return fortType.isCannon();
    }

    public boolean isStation() {

        return fortType.isStation();
    }

    @Override
    public boolean canAttack(BattleElement target) {

        Node from = getNode();
        Node to = target.getNode();

        int distance = from.getHeuristic(to);

        return distance >= 0 && distance <= getRange();

    }

    public boolean isDestroyed() {

        return health <= 0;
    }

}

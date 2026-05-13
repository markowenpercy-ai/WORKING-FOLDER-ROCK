package com.go2super.service.battle.type;

import com.go2super.database.entity.sub.BattleFleet;
import com.go2super.logger.BotLogger;
import com.go2super.service.battle.comparator.*;
import lombok.Getter;

public enum TargetInterval {

    CLOSEST(true),

    DEFENSIVE_BUILDINGS(false),
    MAX_ATTACK_POWER(true),
    MIN_ATTACK_POWER(true),

    MAX_DURABILITY(true),
    MIN_DURABILITY(true),

    COMMANDER(true),

    ;

    @Getter
    private final boolean fleetsPriority;

    TargetInterval(boolean fleetsPriority) {

        this.fleetsPriority = fleetsPriority;
    }

    public IntervalComparator getComparator(BattleFleet attacker) {

        switch (this) {
            case CLOSEST:
                return new IntervalClosestComparator(attacker);
            case COMMANDER:
                return new IntervalCommanderComparator(attacker);
            case MAX_ATTACK_POWER:
                return new IntervalMaxAttackComparator(attacker);
            case MIN_ATTACK_POWER:
                return new IntervalMinAttackComparator(attacker);
            case MAX_DURABILITY:
                return new IntervalMaxDurabilityComparator(attacker);
            case MIN_DURABILITY:
                return new IntervalMinDurabilityComparator(attacker);
            case DEFENSIVE_BUILDINGS:
                return new IntervalBuildingsComparator(attacker);
        }

        BotLogger.error("IntervalComparator for " + this + " was not implemented yet! Using default fallback...");
        return new IntervalClosestComparator(attacker);

    }

}

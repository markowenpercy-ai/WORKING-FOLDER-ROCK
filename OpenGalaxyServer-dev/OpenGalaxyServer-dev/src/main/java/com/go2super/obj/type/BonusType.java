package com.go2super.obj.type;

public enum BonusType {

    NONE(0, false),
    PLANET_APPEARANCE(0, false),

    ORDINARY_PLANET(0, false),
    CONSTRUCTION_SLOTS(0, false),
    PLANET_PROTECTION(0, false),
    TRUCE_IMPEDIMENT(0, false),

    MVP_RESOURCE_PRODUCTION(0.2, false),
    MVP_SHIP_BUILDING_RATE(0.2, false),
    MVP_SHIP_REPAIRING_RATE(0.2, false),
    MVP_CONSTRUCTION_SPEED(0.2, false),
    MVP_DAILY_DRAWS_BONUS(0.2, false),

    GF_RESOURCE_PRODUCTION(0.05, false),
    GF_SHIP_BUILDING_SPEED(0.05, false),
    GF_SHIP_REPAIRING_SPEED(0.5, false),

    HALLOWEEN_RESOURCE_PRODUCTION(0.15, false),
    HALLOWEEN_SHIP_BUILDING_RATE(0.15, false),

    CHRISTMAS_SHIP_BUILDING_SPEED(0.15, false),
    CHRISTMAS_RESOURCE_PRODUCTION(0.15, false),

    LUXURIOUS_GOLD_RESOURCE_PRODUCTION(0.05, false),
    METALLIC_METAL_RESOURCE_PRODUCTION(0.05, false),
    GASEOUS_HE3_RESOURCE_PRODUCTION(0.05, false),

    BASIC_HE3_RESOURCE_PRODUCTION(0.3, false),
    BASIC_METAL_RESOURCE_PRODUCTION(0.3, false),
    BASIC_GOLD_RESOURCE_PRODUCTION(0.3, false),

    ADVANCED_GOLD_RESOURCE_PRODUCTION(1, false),

    ;

    private final double delta;
    private final boolean stackable;

    BonusType(double delta, boolean stackable) {

        this.delta = delta;
        this.stackable = stackable;
    }

    public boolean isStackable() {

        return stackable;
    }

    public double delta() {

        return delta;
    }

}

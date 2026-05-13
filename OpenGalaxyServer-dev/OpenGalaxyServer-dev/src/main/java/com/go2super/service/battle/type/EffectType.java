package com.go2super.service.battle.type;

import lombok.Getter;

public enum EffectType {

    NONE(false, false),

    ALICIA(false, false), // * Alice skill
    JEROME(true, false),
    REGGIE(false, false),
    HELOYCE(false, false),
    VINNA(false, false),
    WAYNE(false, false),
    RASLIN(false, false),
    ROCKY(false, false),
    RINGEL(true, true),
    BART(false, false),
    AILEEN(false, false),
    CIRCE(false, false),
    CALLISTO(true, false),
    BAIN(false, false),
    CASSIUS(false, false),
    MEDUSA(false, false),
    STANI(false, false),
    SLAYER_BAEL(false, false),
    LUNA_SILVESTRI(false, false),
    CHROME_DOME(false, false),
    SHADOW_COUNTESS(false, false),
    MURPHY_LAWSON(false, false),

    PARTICLE_IMPACT_TECH(false, false), // * Directional Tech
    RADIATIVE_INTERFERENCE(false, true), // * Directional Tech
    ELECTRONIC_INTERFERENCE(false, true), // * Directional Tech
    DYNAMIC_IMPAIRMENT(false, true), // * Directional Tech

    Eva(true, false)
    ;

    @Getter
    private final boolean stackable;

    EffectType(boolean stackable, boolean additive) {

        this.stackable = stackable;
    }

}
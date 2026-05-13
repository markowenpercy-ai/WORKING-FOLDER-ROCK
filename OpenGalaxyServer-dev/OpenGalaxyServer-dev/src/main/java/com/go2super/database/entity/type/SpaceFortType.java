package com.go2super.database.entity.type;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.FortificationData;
import com.go2super.resources.data.meta.FortificationLevelMeta;
import lombok.Getter;

public enum SpaceFortType {

    COMMON_SPACE_STATION(0, 13, 3, true, true),
    COMMON_METEOR_STAR(1, 15, 3, false, true),

    COMMON_PARTICLE_CANNON(2, 16, 1, true, true),
    COMMON_ANTI_AIRCRAFT_CANNON(3, 17, 2, true, true),
    COMMON_THOR_CANNON(4, 18, 3, true, true),

    RBP_SPACE_STATION(0, 13, 3, true, false),
    RBP_METEOR_STAR(1, 15, 3, false, false),

    RBP_PARTICLE_CANNON(2, 16, 1, true, false),
    RBP_ANTI_AIRCRAFT_CANNON(3, 17, 2, true, false),
    RBP_THOR_CANNON(4, 18, 3, true, false),

    ;

    @Getter
    private final int code;
    @Getter
    private final int dataId;
    @Getter
    private final int buildType;

    @Getter
    private final boolean cannon;
    @Getter
    private final boolean common;

    SpaceFortType(int code, int dataId, int buildType, boolean cannon, boolean common) {

        this.code = code;
        this.cannon = cannon;
        this.buildType = buildType;
        this.common = common;
        this.dataId = dataId;
    }

    public boolean isStation() {

        return this == COMMON_SPACE_STATION || this == RBP_SPACE_STATION;
    }

    public FortificationLevelMeta getData(int levelId) {

        return getData().getLevels().stream().filter(meta -> meta.getLv() == levelId).findFirst().orElse(null);
    }

    public FortificationData getData() {

        if (isCommon()) {

            for (FortificationData fortificationData : ResourceManager.getFortification().getFortifications()) {
                if (fortificationData.getId() == getDataId()) {
                    return fortificationData;
                }
            }

        } else {

            if (this == RBP_SPACE_STATION) {
                return ResourceManager.getRBPss().getFortification();
            }

            for (FortificationData fortificationData : ResourceManager.getRBPFortification().getFortifications()) {
                if (fortificationData.getId() == getDataId()) {
                    return fortificationData;
                }
            }

        }

        return null;

    }

    public static SpaceFortType getFortType(boolean common, int dataId) {

        for (SpaceFortType type : SpaceFortType.values()) {
            if (type.isCommon() == common && type.getDataId() == dataId) {
                return type;
            }
        }
        return null;
    }

}

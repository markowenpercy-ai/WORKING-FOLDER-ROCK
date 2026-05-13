package com.go2super.database.entity.sub;

import com.go2super.obj.game.BuildInfo;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.BuildData;
import com.go2super.resources.data.meta.BuildLevelMeta;
import com.go2super.socket.util.DateUtil;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserBuilding {

    private Boolean repairing;
    private Date untilRepair;

    private Boolean updating;
    private Date untilUpdate;

    private int index = -1;
    private int levelId;
    private int buildingId;

    private int x;
    private int y;

    public Long updatingTime() {

        if (updating == null || !updating || untilUpdate == null) {
            return 0L;
        }

        return DateUtil.remains(untilUpdate);

    }

    public Long repairingTime() {

        if (repairing == null || !repairing || untilRepair == null) {
            return 0L;
        }

        return DateUtil.remains(untilRepair);

    }

    public Long spareTime() {

        Long repairingTime = repairingTime();

        if (repairingTime != 0) {
            return repairingTime;
        }

        return updatingTime();

    }

    public BuildLevelMeta getLevelData() {

        return getData().getLevel(levelId);
    }

    public BuildData getData() {

        return ResourceManager.getBuilds().getBuild(buildingId);

    }

    public BuildInfo getInfo(int spareTime) {

        return new BuildInfo(spareTime, x, y, index, buildingId, levelId);
    }

}

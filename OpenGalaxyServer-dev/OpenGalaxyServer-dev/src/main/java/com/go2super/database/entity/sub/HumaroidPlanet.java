package com.go2super.database.entity.sub;

import com.go2super.database.entity.Planet;
import com.go2super.database.entity.type.PlanetType;
import com.go2super.obj.utility.GalaxyTile;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.InstanceData;
import com.go2super.service.BattleService;
import com.go2super.socket.util.MathUtil;
import lombok.Data;

import java.util.*;

@Data
public class HumaroidPlanet extends Planet {

    private int currentCorp;
    private int currentLevel;

    private boolean destroyed;

    private boolean peace;
    private Date statusTime;

    public HumaroidPlanet() {

    }

    public HumaroidPlanet(GalaxyTile galaxyTile, long userId) {

        this.currentLevel = MathUtil.random(0, 15);
        this.statusTime = new Date();

        this.setUserId(userId);
        this.setType(PlanetType.HUMAROID_PLANET);
        this.setPosition(galaxyTile);

    }

    public InstanceData getData() {

        return ResourceManager.getHumaroids().getHumaroid(currentLevel);
    }

    public boolean hasFight() {

        return BattleService.getInstance().isInWar(this);
    }

    public boolean hasTruce() {

        if (statusTime == null || destroyed) {
            return true;
        }
        return peace && statusTime.getTime() > new Date().getTime();
    }

}

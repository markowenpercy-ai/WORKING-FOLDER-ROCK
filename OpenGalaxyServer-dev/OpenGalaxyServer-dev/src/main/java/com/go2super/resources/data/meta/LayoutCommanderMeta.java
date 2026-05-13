package com.go2super.resources.data.meta;

import com.go2super.database.entity.Commander;
import com.go2super.database.entity.sub.BattleCommander;
import com.go2super.logger.BotLogger;
import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.CommanderStatsData;
import com.go2super.service.AutoIncrementService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LayoutCommanderMeta extends JsonData {

    private String name;

    private int rank;
    private int lv;
    private int accuracy;
    private int dodge;
    private int speed;
    private int electron;

    public BattleCommander getBattleCommander(boolean save, EnemyStatsMeta stats) {

        CommanderStatsData statsData = getData();

        Commander virtualCommander = new Commander();

        // System.out.println("Name: " + name);

        virtualCommander.setSkill(statsData.getId());
        virtualCommander.setName(name);
        virtualCommander.setStars(rank);
        virtualCommander.setLevel(lv);
        virtualCommander.setExperience(-1);
        virtualCommander.setVariance(30);

        if (stats == null) {

            virtualCommander.setGrowthDodge(lv);
            virtualCommander.setGrowthElectron(lv);
            virtualCommander.setGrowthSpeed(lv);
            virtualCommander.setGrowthAim(lv);

        } else {

            virtualCommander.setGrowthDodge(0);
            virtualCommander.setGrowthElectron(0);
            virtualCommander.setGrowthSpeed(0);
            virtualCommander.setGrowthAim(0);

        }

        if (save) {

            virtualCommander.setUserId(-1);
            virtualCommander.setCommanderId(AutoIncrementService.getInstance().getNextCommanderId());

            virtualCommander.save();

        } else {

            virtualCommander.setCommanderId(-1);

        }

        return virtualCommander.createBattleCommander(0.0d, stats);

    }

    public CommanderStatsData getData() {

        //for(ResourceManager.getCommanders().get)

        for (CommanderStatsData statsData : ResourceManager.getCommanders().getCommander()) {
            // BotLogger.log(statsData);
            if (statsData.getName().equals(name)) {
                return statsData;
            }
        }

        BotLogger.error("Commander not found " + name);
        return null;

    }

}

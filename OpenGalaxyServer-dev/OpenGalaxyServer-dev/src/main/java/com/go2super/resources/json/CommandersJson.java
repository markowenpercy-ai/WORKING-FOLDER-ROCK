package com.go2super.resources.json;

import com.go2super.resources.data.*;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class CommandersJson {

    private List<CommanderStatsData> commander;
    private List<CommanderLevelsData> levels;
    private List<CommanderProbabilityData> probabilities;
    private List<CommanderCureData> cures;
    private List<CommanderPullulateData> pullulates;

    public int getMaxLevelExp(int level) {

        for (int i = 0; i < level; i++) {
            if (i - 1 == level) {
                return level;
            }
        }
        return 0;
    }

    public CommanderStatsData getCommander(String name) {

        for (CommanderStatsData statsData : commander) {
            if (statsData.getName().equals(name)) {
                return statsData;
            }
        }
        return null;
    }

    public CommanderPullulateData getPullulate(int commanderStar, int commanderType) {

        for (CommanderPullulateData commanderPullulateData : pullulates) {
            if (commanderPullulateData.getCommandStar() == commanderStar + 1 &&
                commanderPullulateData.getCommandType() == commanderType) {
                return commanderPullulateData;
            }
        }
        return null;
    }

}

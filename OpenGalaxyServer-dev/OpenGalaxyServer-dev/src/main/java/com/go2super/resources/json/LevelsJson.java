package com.go2super.resources.json;

import com.go2super.resources.data.LevelData;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
public class LevelsJson {

    private List<LevelData> levels;

    public int getMaxLevelExp(int level) {

        for (LevelData data : levels) {
            if (data.getLevel() - 1 == level) {
                return data.getExp();
            }
        }
        return 0;
    }

    public LevelData getData(int level) {

        for (LevelData data : levels) {
            if (data.getLevel() - 1 == level) {
                return data;
            }
        }
        return null;
    }

}

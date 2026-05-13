package com.go2super.obj.game;

import com.go2super.resources.data.CommanderLevelsData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommanderLevel {

    private CommanderLevelsData levelData;

    private int level;
    private int levelExperience;

}

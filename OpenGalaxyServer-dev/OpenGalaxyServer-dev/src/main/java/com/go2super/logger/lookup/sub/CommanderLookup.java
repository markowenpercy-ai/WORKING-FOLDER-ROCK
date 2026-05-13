package com.go2super.logger.lookup.sub;

import lombok.Builder;
import lombok.Data;

import java.util.*;

@Data
@Builder
public class CommanderLookup {

    private String commanderName;
    private int commanderId;
    private int shipTeamId;

    private long userId;

    private int level;
    private int experience;
    private int stars;
    private int variance;

    private double growthElectron;
    private double growthAccuracy;
    private double growthSpeed;
    private double growthDodge;

    private List<CommanderChipLookup> chips;
    private List<CommanderGemLookup> gems;

}

package com.go2super.resources.json;

import com.go2super.resources.data.ChipData;
import com.go2super.resources.data.meta.ChipRewardMeta;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ChipsJson {

    private List<ChipData> chips;

    public ChipData getLevel(int level) {

        for (ChipData chip : chips) {
            if (chip.getLv() == level) {
                return chip;
            }
        }
        return null;
    }

    public ChipRewardMeta pickOne(int level, boolean corsair) {

        ChipData chipLevel = chips.stream().filter(chipData -> chipData.getLv() == level).findFirst().orElse(null);
        if (chipLevel == null) {
            return null;
        }

        return chipLevel.pickOne(corsair);

    }

}

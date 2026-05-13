package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.ChipMoneyMeta;
import com.go2super.resources.data.meta.ChipRewardMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.security.SecureRandom;
import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChipData extends JsonData {

    private int lv;
    private int next;
    private ChipMoneyMeta price;

    private List<ChipRewardMeta> rewards;

    public ChipRewardMeta pickOne(boolean corsair) {

        float weightSum = 0F;
        for (int i = 0; i < rewards.size(); i++) {
            weightSum += corsair ? rewards.get(i).getChances().getCorsair() : rewards.get(i).getChances().getMp();
        }

        float random = new SecureRandom().nextFloat() * weightSum;

        float lowerRangeLimit = 0;
        float upperRangeLimit;

        for (int i = 0; i < rewards.size(); i++) {

            upperRangeLimit = lowerRangeLimit + (corsair ? rewards.get(i).getChances().getCorsair() : rewards.get(i).getChances().getMp());

            if (random < upperRangeLimit) {
                return rewards.get(i);
            }

            lowerRangeLimit = upperRangeLimit;

        }

        Collections.shuffle(rewards);
        return rewards.get(0);

    }

}

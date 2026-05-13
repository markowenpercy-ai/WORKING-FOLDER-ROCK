package com.go2super.resources.json;

import com.go2super.resources.data.LotteryData;
import lombok.Data;
import lombok.ToString;

import java.security.SecureRandom;
import java.util.*;

@Data
@ToString
public class LotteryJson {

    private List<LotteryData> lottery;

    public LotteryData pickOne() {

        float weightSum = 0F;
        for (int i = 0; i < lottery.size(); i++) {
            weightSum += lottery.get(i).getWeight();
        }

        float random = new SecureRandom().nextFloat() * weightSum;

        float lowerRangeLimit = 0;
        float upperRangeLimit;

        for (int i = 0; i < lottery.size(); i++) {

            upperRangeLimit = lowerRangeLimit + lottery.get(i).getWeight();

            if (random < upperRangeLimit) {
                return lottery.get(i);
            }

            lowerRangeLimit = upperRangeLimit;

        }

        Collections.shuffle(lottery);
        return lottery.get(0);

    }

}

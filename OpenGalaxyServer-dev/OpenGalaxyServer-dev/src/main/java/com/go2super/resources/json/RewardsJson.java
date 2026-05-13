package com.go2super.resources.json;

import com.go2super.resources.data.RewardData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class RewardsJson {

    private List<RewardData> rewards;

    public RewardData getReward(int level) {

        for (RewardData reward : rewards) {
            if (reward.getLevel() == level) {
                return reward;
            }
        }
        return null;
    }

}

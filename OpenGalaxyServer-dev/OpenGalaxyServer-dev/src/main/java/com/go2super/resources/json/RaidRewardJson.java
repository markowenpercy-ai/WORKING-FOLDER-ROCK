package com.go2super.resources.json;

import com.go2super.resources.data.RaidRewardData;
import com.go2super.resources.data.meta.RaidRewardMeta;
import lombok.Data;
import lombok.ToString;

import java.util.List;
import java.util.Random;

@Data
@ToString
public class RaidRewardJson {
    private List<RaidRewardData> raidReward;

    public RaidRewardMeta getReward(int propId){
        Random random = new Random();
        for (var ruin: raidReward) {
            if(ruin.getPropId() == propId){
                List<RaidRewardMeta> rewards = ruin.getReward();
                double chance = random.nextDouble() * 100.0;
                double cumulative = 0.0;
                for (var reward: rewards){
                    cumulative += reward.getRate();
                    if (chance <= cumulative)
                        return reward;
                }
            }
        }
        return null;
    }
}

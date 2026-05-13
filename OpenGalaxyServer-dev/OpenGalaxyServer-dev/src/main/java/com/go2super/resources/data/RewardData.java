package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.EZRewardMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RewardData extends JsonData {

    private int level;
    private int time;

    private EZRewardMeta reward;

}

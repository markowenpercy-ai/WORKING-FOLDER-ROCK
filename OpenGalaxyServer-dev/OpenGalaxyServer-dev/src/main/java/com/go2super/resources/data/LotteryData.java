package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.LotteryRewardMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LotteryData extends JsonData {

    private int id;
    private int position;
    private String name;
    private boolean broadcast;
    private int weight;

    private LotteryRewardMeta reward;

}

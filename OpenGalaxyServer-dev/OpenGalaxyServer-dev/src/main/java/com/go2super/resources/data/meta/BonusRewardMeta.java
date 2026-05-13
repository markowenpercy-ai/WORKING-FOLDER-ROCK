package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BonusRewardMeta extends JsonData {

    private String prop;

    private int chance;  // percentage, e.g., 4 = 4% chance
}
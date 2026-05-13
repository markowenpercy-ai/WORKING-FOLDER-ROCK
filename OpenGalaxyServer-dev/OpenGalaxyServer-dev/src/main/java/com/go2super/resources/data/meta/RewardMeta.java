package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RewardMeta extends JsonData {

    private String prop;

    private int num;
    private int weight;
    private boolean one;
    private String group = "Default";
}

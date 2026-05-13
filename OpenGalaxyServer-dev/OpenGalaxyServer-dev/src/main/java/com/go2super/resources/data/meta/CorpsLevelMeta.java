package com.go2super.resources.data.meta;

import com.go2super.resources.JsonData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CorpsLevelMeta extends JsonData {

    private int lv;
    private int time;
    private int cost;

    private List<CorpsLevelEffectMeta> effects;


}

package com.go2super.resources.data.meta;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResearchLevelMeta {

    private int lv;
    private double gold;
    private double time;

    private List<ResearchEffectMeta> effects;

}

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
public class RBPSSLevelMeta {

    private int lv;
    private List<FortificationEffectMeta> effects;

}

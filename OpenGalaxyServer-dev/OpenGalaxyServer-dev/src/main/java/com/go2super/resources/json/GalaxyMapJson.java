package com.go2super.resources.json;

import com.go2super.resources.data.GalaxyMapData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class GalaxyMapJson {

    private List<GalaxyMapData> player;
    private List<GalaxyMapData> humaroid;
    private List<GalaxyMapData> corpBonus;

}

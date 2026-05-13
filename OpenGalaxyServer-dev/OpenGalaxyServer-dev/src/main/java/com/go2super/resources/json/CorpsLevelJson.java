package com.go2super.resources.json;

import com.go2super.resources.data.CorpsLevelData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class CorpsLevelJson {

    private List<CorpsLevelData> corpsUpgrades;

}

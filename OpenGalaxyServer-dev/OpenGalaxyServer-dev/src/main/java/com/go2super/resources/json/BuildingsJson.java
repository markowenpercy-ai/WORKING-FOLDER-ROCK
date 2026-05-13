package com.go2super.resources.json;

import com.go2super.resources.data.LevelData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class BuildingsJson {

    private List<LevelData> levels;

}

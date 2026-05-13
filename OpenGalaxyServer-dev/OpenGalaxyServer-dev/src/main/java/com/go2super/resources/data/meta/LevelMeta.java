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
public class LevelMeta extends JsonData {

    private String imageName;
    private int levelId;
    private String occupationSpace;
    private String levelComment;
    private int time;
    private int costMetal;
    private int costHelium3;
    private int costFunds;
    private int metalResourcesNum;
    private int he3resourcesNum;

}

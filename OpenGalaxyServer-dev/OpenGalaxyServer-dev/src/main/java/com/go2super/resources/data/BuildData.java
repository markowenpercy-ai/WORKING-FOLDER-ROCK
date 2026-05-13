package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.BuildIfMeta;
import com.go2super.resources.data.meta.BuildLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuildData extends JsonData {

    private int id;

    private String name;
    private String type;

    private int limit;
    private List<BuildIfMeta> limitIf;
    private List<BuildLevelMeta> levels;

    public BuildLevelMeta getLevel(int level) {

        for (BuildLevelMeta meta : levels) {
            if (meta.getLv() == level) {
                return meta;
            }
        }
        return null;
    }

}

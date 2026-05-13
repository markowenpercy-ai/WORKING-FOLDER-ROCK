package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.FortificationLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FortificationData extends JsonData {

    private int id;
    private List<FortificationLevelMeta> levels;

    public FortificationLevelMeta getLevel(int lv) {

        return levels.stream().filter(level -> level.getLv() == lv).findFirst().orElse(null);
    }

}

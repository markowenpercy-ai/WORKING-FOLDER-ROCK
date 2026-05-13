package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.CorpsLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CorpsLevelData extends JsonData {

    private int id;
    private String name;

    private List<CorpsLevelMeta> levels;

}

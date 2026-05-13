package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.data.meta.FlagshipRequirementMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FlagshipData extends JsonData {

    private int propId;
    private List<FlagshipRequirementMeta> required;

    private int money;

    public boolean isRandom() {

        return propId == -1;
    }

}

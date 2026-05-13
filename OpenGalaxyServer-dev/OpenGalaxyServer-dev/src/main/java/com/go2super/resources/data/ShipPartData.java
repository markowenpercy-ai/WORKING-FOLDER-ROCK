package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.meta.PartLevelMeta;
import com.go2super.resources.data.meta.UpgradeMeta;
import com.go2super.resources.data.props.PropPartData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ShipPartData extends JsonData {

    private int groupId;
    private String name;

    private String partType;
    private String partSubType;
    private String partOperation;

    private int limit;
    private List<PartLevelMeta> levels;

    private UpgradeMeta upgrade;

    public PartLevelMeta getLevel(int id) {

        for (PartLevelMeta meta : levels) {
            if (meta.getId() == id) {
                return meta;
            }
        }
        return null;
    }

    public PropPartData getPropData() {

        return ResourceManager.getProps().getPartData(name).orElse(null);
    }

}

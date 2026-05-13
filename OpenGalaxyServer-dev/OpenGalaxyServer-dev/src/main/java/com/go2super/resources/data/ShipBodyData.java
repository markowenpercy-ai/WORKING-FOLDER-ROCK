package com.go2super.resources.data;

import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.props.PropBodyData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ShipBodyData extends JsonData {

    private int groupId;

    private String name;
    private String bodyType;
    private String armorType;

    private List<BodyLevelMeta> levels;

    public BodyLevelMeta getLevel(int id) {

        for (BodyLevelMeta meta : levels) {
            if (meta.getId() == id) {
                return meta;
            }
        }
        return null;
    }

    public boolean isFlagship() {

        return bodyType.equals("flagship");
    }

    public PropBodyData getPropData() {

        return ResourceManager.getProps().getBodyData(name).orElse(null);
    }

}

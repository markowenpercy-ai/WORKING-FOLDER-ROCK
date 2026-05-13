package com.go2super.resources.json;

import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartEffectMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ShipPartJson {

    private Map<Integer, ShipPartData> cachedParts = new HashMap<>();
    private List<ShipPartData> shipPart;

    public ShipPartData findByPartName(String name) {

        for (ShipPartData data : shipPart) {
            if (data.getName().equals(name)) {
                return data;
            }
        }

        return null;

    }

    public ShipPartData findByGroupId(int groupId) {

        for (ShipPartData data : shipPart) {
            if (data.getGroupId() == groupId) {
                return data;
            }
        }

        return null;

    }

    public ShipPartData findByPartId(int id) {

        if (cachedParts.containsKey(id)) {
            return cachedParts.get(id);
        }

        for (ShipPartData data : shipPart) {
            for (PartLevelMeta meta : data.getLevels()) {
                if (meta.getId() == id) {
                    cachedParts.put(id, data);
                    return data;
                }
            }
        }

        return null;

    }

    public PartEffectMeta getEffect(int id, String attribute) {

        return getMeta(id).getEffect(attribute);
    }

    public PartLevelMeta getMeta(int id) {

        ShipPartData partData = findByPartId(id);
        if (partData != null) {
            return partData.getLevel(id);
        }
        return null;
    }

}

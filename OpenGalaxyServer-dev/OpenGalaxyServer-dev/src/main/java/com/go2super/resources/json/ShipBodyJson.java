package com.go2super.resources.json;

import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ShipBodyJson {

    private Map<Integer, ShipBodyData> cachedBodies = new HashMap<>();
    private List<ShipBodyData> shipBody;

    public ShipBodyData findByBodyName(String name) {

        for (ShipBodyData data : shipBody) {
            if (data.getName().equals(name)) {
                return data;
            }
        }

        return null;

    }

    public ShipBodyData findByBodyId(int id) {

        if (cachedBodies.containsKey(id)) {
            return cachedBodies.get(id);
        }

        for (ShipBodyData data : shipBody) {
            for (BodyLevelMeta meta : data.getLevels()) {
                if (meta.getId() == id) {
                    cachedBodies.put(id, data);
                    return data;
                }
            }
        }

        return null;

    }

    public BodyLevelMeta getMeta(int id) {

        ShipBodyData shipBodyData = findByBodyId(id);
        if (shipBodyData != null) {
            return shipBodyData.getLevel(id);
        }
        return null;
    }

}

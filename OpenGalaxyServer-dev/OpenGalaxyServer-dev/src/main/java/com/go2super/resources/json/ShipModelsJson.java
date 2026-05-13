package com.go2super.resources.json;

import com.go2super.resources.data.DefaultModelData;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class ShipModelsJson {

    private List<DefaultModelData> models;

    public DefaultModelData lookupDefaultId(String nameKey) {

        for (DefaultModelData model : models) {
            if (model.getName().equalsIgnoreCase(nameKey)) {
                return model;
            }
        }
        return models.get(0);
    }

}

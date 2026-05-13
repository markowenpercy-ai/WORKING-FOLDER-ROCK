package com.go2super.resources.data.meta;

import com.go2super.database.entity.ShipModel;
import com.go2super.resources.JsonData;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.DefaultModelData;
import com.go2super.service.PacketService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LayoutDesignMeta extends JsonData {

    private String design;
    private int amount;

    public ShipModel getModel() {

        DefaultModelData defaultModel = getDefaultModel();

        if (defaultModel == null) {
            return PacketService.getShipModel(0);
        }

        return PacketService.getShipModel(defaultModel.getId());

    }

    public DefaultModelData getDefaultModel() {

        return ResourceManager.getShipModels().lookupDefaultId(design);
    }

}

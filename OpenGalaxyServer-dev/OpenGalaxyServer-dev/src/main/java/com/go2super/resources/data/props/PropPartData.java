package com.go2super.resources.data.props;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropPartData extends PropMetaData {

    private String part;
    private Integer[] requirement;

    public boolean canUse(List<Integer> currentParts) {

        if (requirement == null) {
            return true;
        }

        for (Integer id : requirement) {

            ShipPartData partData = ResourceManager.getShipParts().findByPartId(id);

            PartLevelMeta requirementLevel = partData.getLevel(id);
            PartLevelMeta userLevel = getUserLevel(currentParts, partData);

            if (requirementLevel == null) {
                continue;
            }
            if (userLevel == null) {
                return false;
            }
            if (requirementLevel.getLv() > userLevel.getLv()) {
                return false;
            }

        }

        return true;

    }

    private PartLevelMeta getUserLevel(List<Integer> currentParts, ShipPartData partData) {

        for (int part : currentParts) {
            if (partData.getLevel(part) != null) {
                return partData.getLevel(part);
            }
        }
        return null;
    }

}

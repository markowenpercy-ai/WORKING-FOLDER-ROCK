package com.go2super.resources.data.props;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PropBodyData extends PropMetaData {

    private String body;
    private Integer[] requirement;

    public boolean canUse(List<Integer> currentBodies) {

        if (requirement == null) {
            return true;
        }

        for (Integer id : requirement) {

            ShipBodyData bodyData = ResourceManager.getShipBodies().findByBodyId(id);

            BodyLevelMeta requirementLevel = bodyData.getLevel(id);
            BodyLevelMeta userLevel = getUserLevel(currentBodies, bodyData);

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

    private BodyLevelMeta getUserLevel(List<Integer> currentBodies, ShipBodyData bodyData) {

        for (int body : currentBodies) {
            if (bodyData.getLevel(body) != null) {
                return bodyData.getLevel(body);
            }
        }
        return null;
    }

}

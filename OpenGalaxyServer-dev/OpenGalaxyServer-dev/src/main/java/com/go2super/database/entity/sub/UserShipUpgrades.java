package com.go2super.database.entity.sub;

import com.go2super.obj.game.ShortArray;
import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ShipBodyData;
import com.go2super.resources.data.ShipPartData;
import com.go2super.resources.data.meta.BodyLevelMeta;
import com.go2super.resources.data.meta.PartLevelMeta;
import lombok.Builder;
import lombok.Data;

import java.util.*;

@Builder
@Data
public class UserShipUpgrades {

    public List<Integer> currentBodies = new ArrayList<>();
    public List<Integer> currentParts = new ArrayList<>();

    public ShipUpgrade shipUpgrade;
    public ShipUpgrade partUpgrade;

    public boolean hasPartUpgrade(int id) {

        ShipPartData data = ResourceManager.getShipParts().findByPartId(id);
        if (data == null) {
            return true;
        }
        PartLevelMeta level = data.getLevel(id);
        boolean has = false;
        for (int some : currentParts) {
            PartLevelMeta userLevel = data.getLevel(some);
            if (userLevel == null) {
                continue;
            }
            if (userLevel.getLv() >= level.getLv()) {
                has = true;
                break;
            }
        }
        return has;
    }

    public boolean hasBodyUpgrade(int id) {

        ShipBodyData data = ResourceManager.getShipBodies().findByBodyId(id);
        if (data == null) {
            return true;
        }
        BodyLevelMeta level = data.getLevel(id);
        boolean has = false;
        for (int some : currentBodies) {
            BodyLevelMeta userLevel = data.getLevel(some);
            if (userLevel == null) {
                continue;
            }
            if (userLevel.getLv() >= level.getLv()) {
                has = true;
                break;
            }
        }
        return has;
    }

    public ShortArray getBodiesArray() {

        int[] array = new int[200];

        for (int i = 0; i < array.length; i++) {
            if (i < currentBodies.size()) {
                array[i] = currentBodies.get(i);
            } else {
                array[i] = 0;
            }
        }

        return new ShortArray(array);

    }

    public ShortArray getPartsArray() {

        int[] array = new int[280];

        for (int i = 0; i < array.length; i++) {
            if (i < currentParts.size()) {
                array[i] = currentParts.get(i);
            } else {
                array[i] = 0;
            }
        }

        return new ShortArray(array);

    }

}

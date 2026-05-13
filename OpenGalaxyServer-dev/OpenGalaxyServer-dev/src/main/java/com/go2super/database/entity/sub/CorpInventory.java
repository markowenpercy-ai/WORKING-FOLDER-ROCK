package com.go2super.database.entity.sub;

import com.go2super.obj.game.Prop;
import lombok.*;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.*;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CorpInventory {

    private int maxStacks;
    private int stackPrice;

    private List<Prop> corpPropList;

    public Prop getProp(int propId) {

        return getProp(propId, 0);
    }

    public Prop getProp(int propId, int storageType) {

        for (Prop prop : corpPropList) {
            if (prop.getPropId() == propId && prop.getStorageType() == storageType) {
                return prop;
            }
        }
        return null;
    }

    public boolean removeOneProp(Prop prop, boolean lock) {

        return removeProp(prop, 1, lock);
    }

    public boolean removeProp(Prop prop, int num, boolean lock) {

        return removeProp(prop.getPropId(), num, prop.getStorageType(), lock);
    }

    public Pair<Boolean, Boolean> removeProp(Prop prop, int num) {

        boolean propLock = prop.getPropLockNum() > 0;
        return propLock ? Pair.of(removeProp(prop, num, true), true) : Pair.of(removeProp(prop, num, false), false);
    }

    public boolean removeProp(int propId, int num, int storageType, boolean lock) {

        Prop prop = getProp(propId, storageType);

        if (prop == null) {
            return false;
        }

        int propNum = lock ? prop.getPropLockNum() : prop.getPropNum();

        if (num > propNum) {
            return false;
        }

        if (lock) {
            prop.setPropLockNum(propNum - num);
        } else {
            prop.setPropNum(propNum - num);
        }

        if (prop.getPropNum() == 0 && prop.getPropLockNum() == 0) {
            corpPropList.remove(prop);
        }

        return true;

    }

    public boolean addProp(Prop prop, int num, boolean lock) {

        return addProp(prop.getPropId(), num, prop.getStorageType(), lock);
    }

    public boolean addProp(int propId, int num, int storageType, boolean lock) {

        Prop prop = getProp(propId, storageType);

        if (prop == null) {

            if (countStacks(storageType) + 1 > maxStacks) {
                return false;
            }

            corpPropList.add(new Prop(propId, lock ? 0 : num, lock ? num : 0, storageType, 0));
            return true;

        }

        int newNum = lock ? prop.getPropLockNum() + num : prop.getPropNum() + num;

        if (newNum > 9999) {
            return false;
        }

        if (lock) {
            prop.setPropLockNum(newNum);
        } else {
            prop.setPropNum(newNum);
        }

        return true;

    }

    public long countStacks(int storageType) {

        long result = 0;
        List<Prop> props = corpPropList.stream().filter(prop -> prop.getStorageType() == storageType).collect(Collectors.toList());

        for (Prop prop : props) {
            if (prop.getPropNum() > 0) {
                result++;
            }
            if (prop.getPropLockNum() > 0) {
                result++;
            }
        }

        return result;

    }

    public CorpInventory debugAddProp(int propId, int propNum, int storageType) {

        corpPropList.add(new Prop(propId, propNum, 0, storageType, 0));
        return this;
    }

    public CorpInventory debugAddProp(int propId, int propNum, int propLockNum, int storageType, int reserve) {

        corpPropList.add(new Prop(propId, propNum, propLockNum, storageType, reserve));
        return this;
    }

}

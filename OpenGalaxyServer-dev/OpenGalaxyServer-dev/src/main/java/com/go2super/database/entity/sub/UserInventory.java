package com.go2super.database.entity.sub;

import com.go2super.obj.game.Prop;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@ToString
public class UserInventory {

    private int maximumStacks;
    private int stackPrice;

    private List<Prop> propList;

    public Prop getProp(int propId) {

        return getProp(propId, 0);
    }

    public Prop getProp(int propId, int storageType) {

        for (Prop prop : propList) {
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
            propList.remove(prop);
        }

        return true;

    }

    public boolean removeProp(int propId, int num, int storageType) {
        Prop prop = getProp(propId, storageType);
        if (prop == null) {
            return false;
        }

        int propNumLocked = prop.getPropLockNum();
        int propNum = prop.getPropNum();
        int totalPropNum = propNumLocked + propNum;

        if (num > totalPropNum) {
            return false;
        }

        if (num > propNumLocked) {
            prop.setPropLockNum(0);
            prop.setPropNum(propNum - (num - propNumLocked));
        } else {
            prop.setPropLockNum(propNumLocked - num);
        }

        if (prop.getPropNum() == 0 && prop.getPropLockNum() == 0) {
            propList.remove(prop);
        }

        return true;

    }

    public boolean hasProp(int propId, int num, int storageType, boolean lock) {

        Prop prop = getProp(propId, storageType);
        if (prop == null) {
            return false;
        }

        int propNum = lock ? prop.getPropLockNum() : prop.getPropNum();
        return num <= propNum;

    }

    public boolean hasProp(int propId, int num, int storageType) {
        Prop prop = getProp(propId, storageType);
        if (prop == null) {
            return false;
        }
        return num <= prop.getPropLockNum() + prop.getPropNum();
    }

    public boolean addProp(Prop prop, int num, boolean lock) {

        return addProp(prop.getPropId(), num, prop.getStorageType(), lock);
    }

    public boolean addProp(int propId, int num, int storageType, boolean lock) {

        Prop prop = getProp(propId, storageType);

        if (prop == null) {

            if (countStacks(storageType) + 1 > maximumStacks) {
                return false;
            }

            propList.add(new Prop(propId, lock ? 0 : num, lock ? num : 0, storageType, 0));
            return true;

        }

        int newNum = Math.min(9999, lock ? prop.getPropLockNum() + num : prop.getPropNum() + num);

        if (lock) {
            prop.setPropLockNum(newNum);
        } else {
            prop.setPropNum(newNum);
        }

        return true;

    }

    public long countStacks(int storageType) {

        long result = 0;
        List<Prop> props = propList.stream().filter(prop -> prop.getStorageType() == storageType).collect(Collectors.toList());

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

    public UserInventory debugAddProp(int propId, int propNum, int storageType) {

        propList.add(new Prop(propId, propNum, 0, storageType, 0));
        return this;
    }

    public UserInventory debugAddProp(int propId, int propNum, int propLockNum, int storageType, int reserve) {

        propList.add(new Prop(propId, propNum, propLockNum, storageType, reserve));
        return this;
    }

}

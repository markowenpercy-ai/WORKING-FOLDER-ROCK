package com.go2super.resources.json;

import com.go2super.resources.data.FortificationData;
import com.go2super.resources.data.meta.FortificationLevelMeta;
import lombok.Data;
import lombok.ToString;

import java.util.*;

@Data
@ToString
public class RBPFortificationJson {

    private List<FortificationData> fortifications;

    public FortificationData getByID(int id) {

        for (FortificationData fortification : fortifications) {
            if (fortification.getId() == id) {
                return fortification;
            }
        }
        return null;
    }

    public FortificationLevelMeta getLevelMeta(int id, int level) {

        FortificationData fortification = getByID(id);
        if (fortification == null) {
            return null;
        }
        if (level >= fortification.getLevels().size()) {
            while (level >= fortification.getLevels().size()) {
                level--;
            }
            return fortification.getLevels().get(level);
        }
        return fortification.getLevels().get(level);
    }

}

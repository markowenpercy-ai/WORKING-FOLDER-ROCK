package com.go2super.resources.json;

import com.go2super.resources.data.ResearchData;
import com.go2super.resources.data.meta.ResearchLevelMeta;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.*;

import java.util.*;

@Data
@ToString
public class ScienceJson {

    private List<ResearchData> construction;
    private List<ResearchData> defense;
    private List<ResearchData> weapons;

    @Getter(value = AccessLevel.PRIVATE)
    @Setter(value = AccessLevel.PRIVATE)
    private List<ResearchData> science;

    public List<ResearchData> getResearchData() {

        if (science == null || science.isEmpty()) {
            science = Lists.newArrayList(Iterables.concat(construction, defense, weapons));
        }
        return science;

    }

    public ResearchData getResearchData(int id) {

        return getResearchData().stream().filter(researchData -> researchData.getId() == id).findFirst().orElse(null);
    }

    public ResearchData getResearchData(String name) {

        return getResearchData().stream().filter(researchData -> researchData.getName().equals(name)).findFirst().orElse(null);
    }

    public ResearchLevelMeta getResearchLevelMeta(int id, int level) {

        return getResearchData(id).getLevel(level);
    }

}

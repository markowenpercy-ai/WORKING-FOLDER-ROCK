package com.go2super.resources.data;

import com.go2super.database.entity.User;
import com.go2super.resources.data.meta.ResearchLevelMeta;
import com.go2super.resources.data.meta.ResearchRequireMeta;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.*;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ResearchData {

    private int id;
    private String name;

    private List<ResearchRequireMeta> require;
    private List<ResearchLevelMeta> levels;

    public ResearchLevelMeta getLevel(int level) {

        return levels.stream().filter(researchLevelMeta -> researchLevelMeta.getLv() == level).findFirst().orElse(null);
    }

    public boolean meetRequirements(User user) {

        if (require == null) {
            return true;
        }
        return require.stream().allMatch(researchRequireMeta -> user.getTechs().getLevel(researchRequireMeta.getId()) >= researchRequireMeta.getLv());
    }

}

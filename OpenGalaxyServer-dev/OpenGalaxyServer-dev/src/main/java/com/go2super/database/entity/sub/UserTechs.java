package com.go2super.database.entity.sub;

import com.go2super.resources.ResourceManager;
import com.go2super.resources.data.ResearchData;
import com.go2super.resources.data.meta.ResearchLevelMeta;
import com.go2super.resources.json.ScienceJson;
import lombok.*;

import java.util.*;

@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@ToString
@Setter
@Getter
public class UserTechs {

    private List<UserTech> techs;
    private TechUpgrade upgrade;

    public boolean has(int id) {

        return getLevel(id) > 0;
    }

    public boolean has(String name) {

        return getLevel(name) > 0;
    }

    public boolean has(int id, int level) {

        return getLevel(id) >= level;
    }

    public boolean has(String name, int level) {

        return getLevel(name) >= level;
    }

    public int getLevel(int id) {

        return techs.stream().filter(tech -> tech.getId() == id).findFirst().orElse(UserTech.builder().id(id).level(0).build()).getLevel();
    }

    public int getLevel(String name) {

        UserTech userTech = getTech(name);
        return userTech == null ? 0 : userTech.getLevel();
    }

    public UserTech getTech(int id) {

        for (UserTech tech : techs) {
            if (tech.getId() == id) {
                return tech;
            }
        }

        return null;

    }

    public UserTech getTech(String name) {

        ScienceJson scienceJson = ResourceManager.getScience();
        ResearchData researchData = scienceJson.getResearchData(name);

        if (researchData == null) {
            return null;
        }
        return getTech(researchData.getId());

    }

    public Optional<ResearchLevelMeta> getMeta(String name) {

        UserTech userTech = getTech(name);
        return userTech == null ? Optional.empty() : Optional.of(userTech.getLevelMeta());

    }

}
